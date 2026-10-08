package com.example.wejam.venue.service;

import com.example.wejam.venue.dto.SpaceRequest;
import com.example.wejam.venue.dto.SpaceResponse;
import com.example.wejam.venue.dto.VenueRequest;
import com.example.wejam.venue.dto.VenueResponse;
import com.example.wejam.venue.dto.VenueSummary;
import com.example.wejam.venue.dto.VenueVerificationRow;
import com.example.wejam.venue.exception.IncompleteVenueException;
import com.example.wejam.venue.exception.SpaceNotFoundException;
import com.example.wejam.venue.exception.VenueNotFoundException;
import com.example.wejam.venue.exception.VerificationStateException;
import com.example.wejam.venue.model.Space;
import com.example.wejam.venue.model.Venue;
import com.example.wejam.venue.repository.SpaceRepository;
import com.example.wejam.venue.repository.VenueRepository;
import com.example.wejam.venue.event.VenueDeletedEvent;
import com.example.wejam.venue.model.VerificationIssue;
import com.example.wejam.venue.model.VerificationStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Limit;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * All authorization lives here (CLAUDE.md §6): the role check for creating, and ownership checks, where a venue
 * someone else owns is reported as not found.
 */
@Service
@Transactional
public class VenueService {

    private final VenueRepository venueRepository;
    private final SpaceRepository spaceRepository;
    private final VenuePhotoService photoService;
    private final List<VenueSubmissionRequirement> submissionRequirements;
    private final ApplicationEventPublisher events;

    public VenueService(VenueRepository venueRepository, SpaceRepository spaceRepository,
                        VenuePhotoService photoService, List<VenueSubmissionRequirement> submissionRequirements,
                        ApplicationEventPublisher events) {
        this.venueRepository = venueRepository;
        this.spaceRepository = spaceRepository;
        this.photoService = photoService;
        this.submissionRequirements = submissionRequirements;
        this.events = events;
    }

    @PreAuthorize("hasRole('VENUE_ADMIN')")
    public VenueResponse create(UUID ownerId, VenueRequest request) {
        Venue venue = new Venue(ownerId);
        apply(venue, request);
        return VenueResponse.from(venueRepository.save(venue), List.of(), List.of());
    }

    /** What a user may see: their own venues in any state; others' only once verified (hidden until then). */
    @Transactional(readOnly = true)
    public VenueResponse getVisible(UUID viewerId, UUID venueId) {
        Venue venue = venueRepository.findById(venueId)
                .filter(v -> v.getOwnerId().equals(viewerId) || v.getVerificationStatus() == VerificationStatus.VERIFIED)
                .orElseThrow(() -> new VenueNotFoundException(venueId));
        return withSpaces(venue);
    }

    /** Any venue, unfiltered: for modules that already checked access (e.g. the platform admin's review). */
    @Transactional(readOnly = true)
    public VenueResponse get(UUID venueId) {
        Venue venue = venueRepository.findById(venueId).orElseThrow(() -> new VenueNotFoundException(venueId));
        return withSpaces(venue);
    }

    public VenueResponse update(UUID ownerId, UUID venueId, VenueRequest request) {
        Venue venue = ownedVenue(ownerId, venueId);
        apply(venue, request);
        return withSpaces(venue);
    }

    /** Its spaces go with it (ON DELETE CASCADE); photo files after commit; other modules via the event. */
    public void delete(UUID ownerId, UUID venueId) {
        Venue venue = ownedVenue(ownerId, venueId);
        photoService.deleteAllOf(venueId);
        venueRepository.delete(venue);
        events.publishEvent(new VenueDeletedEvent(venueId));
    }

    /** For modules that attach data to a venue (e.g. documents): 404 unless owned, and its current status. */
    @Transactional(readOnly = true)
    public VerificationStatus ownedVenueStatus(UUID ownerId, UUID venueId) {
        return ownedVenue(ownerId, venueId).getVerificationStatus();
    }

    @Transactional(readOnly = true)
    public List<VenueSummary> myVenues(UUID ownerId) {
        return venueRepository.findSummariesByOwnerId(ownerId);
    }

    /** Owner: send a finished draft for review (design 7.3). 400 lists everything still missing. */
    public VenueResponse submit(UUID ownerId, UUID venueId) {
        Venue venue = ownedVenue(ownerId, venueId);
        if (venue.getVerificationStatus() != VerificationStatus.DRAFT) {
            throw VerificationStateException.notDraft();
        }
        Map<String, String> missing = missingLicenseDetails(venue.getFssaiNumber(), venue.getFssaiLicenseName(),
                venue.getFssaiValidUntil());
        if (venue.getFssaiValidUntil() != null && venue.getFssaiValidUntil().isBefore(LocalDate.now())) {
            missing.put("fssaiValidUntil", "has passed; enter the date on your renewed license");
        }
        if (venue.getPhotoCount() < VenuePhotoService.MIN_PHOTOS_TO_SUBMIT) {
            missing.put("photos", "add at least %d (you have %d)"
                    .formatted(VenuePhotoService.MIN_PHOTOS_TO_SUBMIT, venue.getPhotoCount()));
        }
        submissionRequirements.forEach(requirement -> missing.putAll(requirement.missing(venueId)));
        if (!missing.isEmpty()) {
            throw new IncompleteVenueException(missing);
        }
        if (venueRepository.submitDraft(venueId, ownerId, VenuePhotoService.MIN_PHOTOS_TO_SUBMIT) == 0) {
            throw VerificationStateException.notDraft(); // submitted, or a photo was deleted, at the same moment
        }
        return get(venueId);
    }

    /** Owner: after fixing what the admin asked for, send a REJECTED venue back to the queue. */
    public VenueResponse resubmit(UUID ownerId, UUID venueId) {
        Venue venue = ownedVenue(ownerId, venueId);
        if (venueRepository.resubmitRejected(venue.getId(), ownerId) == 0) {
            throw VerificationStateException.notRejected();
        }
        return get(venueId);
    }

    // --- Verification decisions, driven by the verification module. Role-checked here too, so no future
    //     caller can skip the check.

    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Transactional(readOnly = true)
    public List<VenueVerificationRow> verificationQueue(VerificationStatus status, int limit) {
        return venueRepository.findVerificationQueue(status, Limit.of(limit));
    }

    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Transactional(readOnly = true)
    public long countOtherVenuesWithFssai(UUID venueId, String fssaiNumber) {
        return venueRepository.countOtherVenuesWithFssai(fssaiNumber, venueId);
    }

    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public void approveVerification(UUID venueId) {
        requireExists(venueId);
        int updated;
        try {
            updated = venueRepository.approvePending(venueId);
        } catch (DataIntegrityViolationException e) {
            // The partial unique index: another venue is already VERIFIED with this FSSAI number.
            throw VerificationStateException.fssaiAlreadyVerified();
        }
        if (updated == 0) {
            throw VerificationStateException.notPending();
        }
    }

    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public void rejectVerification(UUID venueId, String reason, Collection<VerificationIssue> issues) {
        requireExists(venueId);
        // A Postgres array literal; enum names contain no commas, quotes or braces, so this is safe to build.
        String issuesLiteral = issues.stream().map(Enum::name).sorted().distinct()
                .collect(Collectors.joining(",", "{", "}"));
        if (venueRepository.rejectPending(venueId, reason, issuesLiteral) == 0) {
            throw VerificationStateException.notPending();
        }
    }

    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Transactional(readOnly = true)
    public UUID ownerOf(UUID venueId) {
        return venueRepository.findById(venueId).map(Venue::getOwnerId)
                .orElseThrow(() -> new VenueNotFoundException(venueId));
    }

    private void requireExists(UUID venueId) {
        if (!venueRepository.existsById(venueId)) {
            throw new VenueNotFoundException(venueId);
        }
    }

    public SpaceResponse addSpace(UUID ownerId, UUID venueId, SpaceRequest request) {
        Space space = new Space(ownedVenue(ownerId, venueId));
        apply(space, request);
        return SpaceResponse.from(spaceRepository.save(space));
    }

    public SpaceResponse updateSpace(UUID ownerId, UUID venueId, UUID spaceId, SpaceRequest request) {
        Space space = ownedSpace(ownerId, venueId, spaceId);
        apply(space, request);
        return SpaceResponse.from(space);
    }

    public void deleteSpace(UUID ownerId, UUID venueId, UUID spaceId) {
        spaceRepository.delete(ownedSpace(ownerId, venueId, spaceId));
    }

    private Venue ownedVenue(UUID ownerId, UUID venueId) {
        return venueRepository.findByIdAndOwnerId(venueId, ownerId)
                .orElseThrow(() -> new VenueNotFoundException(venueId));
    }

    private Space ownedSpace(UUID ownerId, UUID venueId, UUID spaceId) {
        Venue venue = ownedVenue(ownerId, venueId);
        return spaceRepository.findByIdAndVenueId(spaceId, venue.getId())
                .orElseThrow(() -> new SpaceNotFoundException(spaceId));
    }

    private VenueResponse withSpaces(Venue venue) {
        List<SpaceResponse> spaces = spaceRepository.findByVenueIdOrderByCreatedAtAscIdAsc(venue.getId())
                .stream().map(SpaceResponse::from).toList();
        return VenueResponse.from(venue, photoService.photosOf(venue.getId()), spaces);
    }

    private static void apply(Venue venue, VenueRequest request) {
        String licenseName = blankToNull(request.fssaiLicenseName());
        if (venue.getVerificationStatus() != VerificationStatus.DRAFT) {
            Map<String, String> missing = missingLicenseDetails(request.fssaiNumber(), licenseName,
                    request.fssaiValidUntil());
            if (!missing.isEmpty()) {
                missing.replaceAll((field, message) -> "can't be removed once submitted");
                throw new IncompleteVenueException(missing);
            }
        }
        venue.updateDetails(request.name().strip(), blankToNull(request.description()),
                request.addressLine().strip(), request.city().strip(), request.latitude(), request.longitude());
        venue.changeHostingMode(request.hostingMode());
        venue.changeLicense(request.fssaiNumber(), licenseName, request.fssaiValidUntil());
    }

    private static Map<String, String> missingLicenseDetails(String number, String licenseName, LocalDate validUntil) {
        Map<String, String> missing = new LinkedHashMap<>();
        if (number == null) {
            missing.put("fssaiNumber", "is required");
        }
        if (licenseName == null) {
            missing.put("fssaiLicenseName", "is required");
        }
        if (validUntil == null) {
            missing.put("fssaiValidUntil", "is required");
        }
        return missing;
    }

    private static void apply(Space space, SpaceRequest request) {
        space.update(request.name().strip(), request.capacity(), request.soundPolicy(), request.soundCurfew(),
                blankToNull(request.houseRules()));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
