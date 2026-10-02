package com.example.wejam.venue.dto;

import com.example.wejam.venue.model.SoundPolicy;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

/** Full replacement on PUT, creation on POST. */
public record SpaceRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Main floor")
        @NotBlank @Size(max = 80) String name,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "60")
        @NotNull @Min(1) @Max(1000) Integer capacity,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull SoundPolicy soundPolicy,
        @Schema(nullable = true, type = "string", example = "23:00",
                description = "Local time (venue time zone) events here must end by; null = no curfew")
        @JsonFormat(pattern = "HH:mm") LocalTime soundCurfew,
        @Schema(nullable = true, example = "18+ after 9 PM. Min. spend ₹300 for audience.",
                description = "Shown on every event at this space")
        @Size(max = 1000) String houseRules) {
}
