package com.example.wejam.venue.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record VenuePhotoDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Permanent public URL; safe to cache")
        String url) {
}
