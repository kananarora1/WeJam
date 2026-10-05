package com.example.wejam.verification.dto;

import com.example.wejam.verification.model.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/** An uploaded document with a short-lived download link (private bucket; the link expires). */
public record DocumentResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) DocumentType type,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String contentType,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long sizeBytes,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant uploadedAt,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String downloadUrl,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant downloadUrlExpiresAt,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true,
                description = "When this file is deleted for good (host IDs); null = kept")
        Instant scheduledDeletionAt) {
}
