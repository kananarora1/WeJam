package com.example.wejam.auth.dto;

import com.example.wejam.auth.model.Role;
import com.example.wejam.auth.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

public record MeResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true,
                description = "E.164; null for non-phone sign-in methods") String phone,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true,
                description = "Null until the user completes their profile") String displayName,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Set<Role> roles) {

    public static MeResponse from(User user) {
        return new MeResponse(user.getId(), user.getPhone(), user.getDisplayName(), user.getRoles());
    }
}
