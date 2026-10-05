package com.example.wejam.verification.controller;

import com.example.wejam.common.web.CurrentUser;
import com.example.wejam.venue.dto.VenueResponse;
import com.example.wejam.venue.model.VerificationStatus;
import com.example.wejam.verification.dto.RejectVerificationRequest;
import com.example.wejam.verification.dto.VenueReviewResponse;
import com.example.wejam.verification.dto.VenueVerificationItem;
import com.example.wejam.verification.service.VenueVerificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Admin")
@RestController
@RequestMapping("/api/v1/admin")
class VenueVerificationController {

    private final VenueVerificationService verificationService;

    VenueVerificationController(VenueVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping("/venue-verifications")
    List<VenueVerificationItem> venueVerificationQueue(
            @RequestParam(defaultValue = "PENDING") VerificationStatus status) {
        return verificationService.queue(status);
    }

    @GetMapping("/venues/{venueId}")
    VenueReviewResponse reviewVenue(@PathVariable UUID venueId) {
        return verificationService.review(venueId);
    }

    @PostMapping("/venues/{venueId}/approve")
    VenueResponse approveVenue(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID venueId) {
        return verificationService.approve(CurrentUser.id(jwt), venueId);
    }

    @PostMapping("/venues/{venueId}/reject")
    VenueResponse rejectVenue(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID venueId,
                              @Valid @RequestBody RejectVerificationRequest request) {
        return verificationService.reject(CurrentUser.id(jwt), venueId, request.reason());
    }
}
