package com.example.wejam.venue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GearItemDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Drum kit")
        @NotBlank @Size(max = 40) String name,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true, example = "5-pc")
        @Size(max = 60) String details) {
}
