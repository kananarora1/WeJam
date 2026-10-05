package com.example.wejam.verification.controller;

import com.example.wejam.common.web.CurrentUser;
import com.example.wejam.host.model.HostVerificationStatus;
import com.example.wejam.verification.dto.HostReviewResponse;
import com.example.wejam.verification.dto.HostVerificationItem;
import com.example.wejam.verification.dto.RejectHostVerificationRequest;
import com.example.wejam.verification.service.HostVerificationService;
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
class HostVerificationController {

    private final HostVerificationService verificationService;

    HostVerificationController(HostVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping("/host-verifications")
    List<HostVerificationItem> hostVerificationQueue(
            @RequestParam(defaultValue = "PENDING") HostVerificationStatus status) {
        return verificationService.queue(status);
    }

    @GetMapping("/hosts/{hostProfileId}")
    HostReviewResponse reviewHost(@PathVariable UUID hostProfileId) {
        return verificationService.review(hostProfileId);
    }

    @PostMapping("/hosts/{hostProfileId}/approve")
    HostReviewResponse approveHost(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID hostProfileId) {
        return verificationService.approve(CurrentUser.id(jwt), hostProfileId);
    }

    @PostMapping("/hosts/{hostProfileId}/reject")
    HostReviewResponse rejectHost(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID hostProfileId,
                                  @Valid @RequestBody RejectHostVerificationRequest request) {
        return verificationService.reject(CurrentUser.id(jwt), hostProfileId, request.reason());
    }
}
