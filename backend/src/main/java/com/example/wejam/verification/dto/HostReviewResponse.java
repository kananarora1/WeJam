package com.example.wejam.verification.dto;

import com.example.wejam.host.dto.HostProfileResponse;
import com.example.wejam.host.model.HostVerificationStatus;
import com.example.wejam.host.model.IdType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

public record HostReviewResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) HostProfileResponse profile,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) String ownerName,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) String ownerPhone,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) HostVerificationStatus status,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) IdType idType,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) String rejectionReason,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) Instant requestedAt,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Uploaded ID sides with 5-minute download links")
        List<DocumentResponse> documents) {
}
