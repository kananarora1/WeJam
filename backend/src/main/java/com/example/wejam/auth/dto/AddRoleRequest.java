package com.example.wejam.auth.dto;

import com.example.wejam.auth.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record AddRoleRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Only HOST or VENUE_ADMIN are accepted")
        @NotNull Role role) {
}
