package com.example.wejam.venue.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/** Row of "my venues"; built directly by the repository query. */
public record VenueSummary(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String name,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String city,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long spaceCount) {
}
