package com.example.wejam.venue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Full replacement on PUT, creation on POST. */
public record VenueRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Amber Room")
        @NotBlank @Size(max = 80) String name,
        @Schema(nullable = true)
        @Size(max = 1000) String description,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "12th Main, HAL 2nd Stage")
        @NotBlank @Size(max = 200) String addressLine,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Bengaluru")
        @NotBlank @Size(max = 80) String city,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "12.9784")
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "77.6408")
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude) {
}
