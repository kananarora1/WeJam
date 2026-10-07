package com.example.wejam.venue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PhotoUploadRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "image/jpeg",
                description = "image/jpeg, image/png or image/webp")
        @NotBlank String contentType,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "843211",
                description = "Exact file size; max 5 MB (compress on the phone first)")
        @NotNull @Min(1) @Max(5_242_880) Long sizeBytes) {
}
