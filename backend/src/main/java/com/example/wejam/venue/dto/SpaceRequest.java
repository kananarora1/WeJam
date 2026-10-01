package com.example.wejam.venue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Full replacement on PUT, creation on POST. Gear order is kept. */
public record SpaceRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Main floor")
        @NotBlank @Size(max = 80) String name,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "60")
        @NotNull @Min(1) @Max(1000) Integer capacity,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Size(max = 30) List<@Valid @NotNull GearItemDto> gear) {
}
