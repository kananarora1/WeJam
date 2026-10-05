package com.example.wejam.verification.dto;

import com.example.wejam.verification.model.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentUploadRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull DocumentType type,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "application/pdf",
                description = "image/jpeg, image/png or application/pdf")
        @NotBlank String contentType,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "482133", description = "Exact file size; max 10 MB")
        @NotNull @Min(1) @Max(10_485_760) Long sizeBytes) {
}
