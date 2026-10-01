package com.example.wejam.auth;

import com.example.wejam.auth.dto.AddRoleRequest;
import com.example.wejam.auth.dto.MeResponse;
import com.example.wejam.auth.dto.TokenResponse;
import com.example.wejam.auth.dto.UpdateProfileRequest;
import com.example.wejam.common.CurrentUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Me")
@RestController
@RequestMapping("/api/v1/me")
class MeController {

    private final UserService userService;
    private final AuthService authService;

    MeController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping
    MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        return userService.getProfile(CurrentUser.id(jwt));
    }

    @PatchMapping
    MeResponse updateProfile(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateDisplayName(CurrentUser.id(jwt), request.displayName());
    }

    @PostMapping("/roles")
    TokenResponse addRole(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AddRoleRequest request) {
        return authService.addRole(CurrentUser.id(jwt), request.role());
    }
}
