package com.example.wejam.verification.dto;

import com.example.wejam.host.model.HostType;
import com.example.wejam.host.model.HostVerificationStatus;
import com.example.wejam.host.model.IdType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record HostVerificationItem(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID hostProfileId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true,
                description = "Group name, or the owner's display name for an individual")
        String displayName,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) HostType type,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) String ownerName,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) IdType idType,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) HostVerificationStatus status,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant requestedAt) {
}
