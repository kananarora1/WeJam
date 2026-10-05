package com.example.wejam.verification.dto;

import com.example.wejam.venue.model.VerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record VenueVerificationItem(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID venueId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String name,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String city,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) String ownerName,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String fssaiNumber,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) VerificationStatus status,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant requestedAt,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) VerificationChecks checks) {
}
