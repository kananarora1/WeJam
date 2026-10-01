package com.example.wejam.venue.service;

import com.example.wejam.venue.dto.SpaceRequest;
import com.example.wejam.venue.dto.SpaceResponse;
import com.example.wejam.venue.dto.VenueRequest;
import com.example.wejam.venue.dto.VenueResponse;
import com.example.wejam.venue.dto.VenueSummary;
import com.example.wejam.venue.exception.SpaceNotFoundException;
import com.example.wejam.venue.exception.VenueNotFoundException;
import com.example.wejam.venue.model.Space;
import com.example.wejam.venue.model.Venue;
import com.example.wejam.venue.repository.SpaceRepository;
import com.example.wejam.venue.repository.VenueRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    public VenueService(VenueRepository venueRepository, SpaceRepository spaceRepository) {
        this.venueRepository = venueRepository;
        this.spaceRepository = spaceRepository;
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
    }

    @Transactional(readOnly = true)
    public List<VenueSummary> myVenues(UUID ownerId) {
        return venueRepository.findSummariesByOwnerId(ownerId);
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
        venue.update(request.name().strip(), blankToNull(request.description()), request.addressLine().strip(),
                request.city().strip(), request.latitude(), request.longitude());
    }

    private static void apply(Space space, SpaceRequest request) {
        space.update(request.name().strip(), request.capacity());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
