package com.example.wejam.venue.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** PUT the file to {@code uploadUrl} with exactly these headers, then confirm. */
public record PhotoUploadResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID photoId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String uploadUrl,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "PUT") String method,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Map<String, String> headers,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant expiresAt) {
}
