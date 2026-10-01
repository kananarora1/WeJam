package com.example.wejam.venue.controller;

import com.example.wejam.common.web.CurrentUser;
import com.example.wejam.venue.dto.SpaceRequest;
import com.example.wejam.venue.dto.SpaceResponse;
import com.example.wejam.venue.dto.VenueRequest;
import com.example.wejam.venue.dto.VenueResponse;
import com.example.wejam.venue.dto.VenueSummary;
import com.example.wejam.venue.service.VenueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Venues")
@RestController
@RequestMapping("/api/v1")
class VenueController {

    private final VenueService venueService;

    VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping("/venues")
    @ResponseStatus(HttpStatus.CREATED)
    VenueResponse createVenue(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody VenueRequest request) {
        return venueService.create(CurrentUser.id(jwt), request);
    }

    /** Readable by any signed-in user: hosts browse venues when requesting a slot. */
    @GetMapping("/venues/{venueId}")
    VenueResponse getVenue(@PathVariable UUID venueId) {
        return venueService.get(venueId);
    }

    @PutMapping("/venues/{venueId}")
    VenueResponse updateVenue(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID venueId,
                              @Valid @RequestBody VenueRequest request) {
        return venueService.update(CurrentUser.id(jwt), venueId, request);
    }

    @DeleteMapping("/venues/{venueId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteVenue(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID venueId) {
        venueService.delete(CurrentUser.id(jwt), venueId);
    }

    @GetMapping("/me/venues")
    List<VenueSummary> myVenues(@AuthenticationPrincipal Jwt jwt) {
        return venueService.myVenues(CurrentUser.id(jwt));
    }

    @PostMapping("/venues/{venueId}/spaces")
    @ResponseStatus(HttpStatus.CREATED)
    SpaceResponse createSpace(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID venueId,
                              @Valid @RequestBody SpaceRequest request) {
        return venueService.addSpace(CurrentUser.id(jwt), venueId, request);
    }

    @PutMapping("/venues/{venueId}/spaces/{spaceId}")
    SpaceResponse updateSpace(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID venueId,
                              @PathVariable UUID spaceId, @Valid @RequestBody SpaceRequest request) {
        return venueService.updateSpace(CurrentUser.id(jwt), venueId, spaceId, request);
    }

    @DeleteMapping("/venues/{venueId}/spaces/{spaceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteSpace(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID venueId, @PathVariable UUID spaceId) {
        venueService.deleteSpace(CurrentUser.id(jwt), venueId, spaceId);
    }
}
