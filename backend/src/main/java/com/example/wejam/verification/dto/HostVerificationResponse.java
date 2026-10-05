package com.example.wejam.verification.dto;

import com.example.wejam.host.model.HostVerificationStatus;
import com.example.wejam.host.model.IdType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/** The owner's own view. Others only ever see {@code verified} on the host profile. */
public record HostVerificationResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Optional perk: NOT_REQUESTED is a normal state, not a problem")
        HostVerificationStatus status,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) IdType idType,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) String rejectionReason,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) Instant requestedAt,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Uploaded ID sides with 5-minute download links and their deletion date")
        List<DocumentResponse> documents) {
}
