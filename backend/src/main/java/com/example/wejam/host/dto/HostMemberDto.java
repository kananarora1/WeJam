package com.example.wejam.host.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record HostMemberDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID userId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) String displayName,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "The profile owner") boolean admin) {
}
