package com.example.wejam.host.controller;

import com.example.wejam.common.web.CurrentUser;
import com.example.wejam.host.dto.HostProfileRequest;
import com.example.wejam.host.dto.HostProfileResponse;
import com.example.wejam.host.service.HostProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Hosts")
@RestController
@RequestMapping("/api/v1")
class HostProfileController {

    private final HostProfileService hostProfileService;

    HostProfileController(HostProfileService hostProfileService) {
        this.hostProfileService = hostProfileService;
    }

    @PutMapping("/me/host-profile")
    HostProfileResponse saveMyHostProfile(@AuthenticationPrincipal Jwt jwt,
                                          @Valid @RequestBody HostProfileRequest request) {
        return hostProfileService.saveMine(CurrentUser.id(jwt), request);
    }

    @GetMapping("/me/host-profile")
    HostProfileResponse getMyHostProfile(@AuthenticationPrincipal Jwt jwt) {
        return hostProfileService.getMine(CurrentUser.id(jwt));
    }

    @GetMapping("/host-profiles/{profileId}")
    HostProfileResponse getHostProfile(@PathVariable UUID profileId) {
        return hostProfileService.get(profileId);
    }
}
