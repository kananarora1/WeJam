package com.example.wejam.venue.dto;

import com.example.wejam.venue.model.HostingMode;
import com.example.wejam.venue.model.SoundPolicy;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

/** Full replacement on PUT, creation on POST. Verification status is never taken from a request. */
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
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "12345678901234",
                description = "FSSAI license number. Changing it resets verification to PENDING.")
        @NotNull @Pattern(regexp = "\\d{14}", message = "must be the 14-digit FSSAI license number") String fssaiNumber,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull HostingMode hostingMode,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull SoundPolicy soundPolicy,
        @Schema(nullable = true, type = "string", example = "22:30",
                description = "Local time after which amplified sound must stop; null = no curfew")
        @JsonFormat(pattern = "HH:mm") LocalTime soundCurfew,
        @Schema(nullable = true, example = "18+ after 9 PM. Minimum spend ₹300 per person.")
        @Size(max = 1000) String houseRules) {
}
