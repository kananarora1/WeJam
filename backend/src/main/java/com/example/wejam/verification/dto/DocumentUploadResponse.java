package com.example.wejam.verification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** PUT the file to uploadUrl with exactly these headers, then call confirm with documentId. */
public record DocumentUploadResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID documentId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String uploadUrl,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "PUT") String method,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Headers that are part of the signature (e.g. Content-Type, Content-Length)")
        Map<String, String> headers,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant expiresAt) {
}
