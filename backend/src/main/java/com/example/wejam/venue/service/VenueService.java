package com.example.wejam.venue.service;

import com.example.wejam.venue.dto.SpaceRequest;
import com.example.wejam.venue.dto.SpaceResponse;
import com.example.wejam.venue.dto.VenueRequest;
import com.example.wejam.venue.dto.VenueResponse;
import com.example.wejam.venue.dto.VenueSummary;
import com.example.wejam.venue.dto.VenueVerificationRow;
import com.example.wejam.venue.exception.VerificationStateException;
import com.example.wejam.venue.exception.SpaceNotFoundException;
import com.example.wejam.venue.exception.VenueNotFoundException;
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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

/**
 * All authorization lives here (CLAUDE.md §6): the role check for creating, and ownership checks, where a venue
 * someone else owns is reported as not found.
 */
@Service
@Transactional
public class VenueService {

    private final VenueRepository venueRepository;
    private final SpaceRepository spaceRepository;
    private final ApplicationEventPublisher events;

    public VenueService(VenueRepository venueRepository, SpaceRepository spaceRepository,
                        ApplicationEventPublisher events) {
        this.venueRepository = venueRepository;
        this.spaceRepository = spaceRepository;
        this.events = events;
    }

    @PreAuthorize("hasRole('VENUE_ADMIN')")
    public VenueResponse create(UUID ownerId, VenueRequest request) {
        Venue venue = new Venue(ownerId);
        apply(venue, request);
        return VenueResponse.from(venueRepository.save(venue), List.of());
    }

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

    /** Its spaces go with it (ON DELETE CASCADE). */
    public void delete(UUID ownerId, UUID venueId) {
        venueRepository.delete(ownedVenue(ownerId, venueId));
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
        return VenueResponse.from(venue, spaces);
    }

    private static void apply(Venue venue, VenueRequest request) {
        venue.updateDetails(request.name().strip(), blankToNull(request.description()),
                request.addressLine().strip(), request.city().strip(), request.latitude(), request.longitude());
        venue.changeHostingMode(request.hostingMode());
        venue.changeFssaiNumber(request.fssaiNumber());
    }

    private static void apply(Space space, SpaceRequest request) {
        space.update(request.name().strip(), request.capacity(), request.soundPolicy(), request.soundCurfew(),
                blankToNull(request.houseRules()));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
