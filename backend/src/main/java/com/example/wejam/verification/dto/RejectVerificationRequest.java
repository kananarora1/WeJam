package com.example.wejam.verification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectVerificationRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                example = "The FSSAI number doesn't match the registry. Please check the 14 digits on your license.",
                description = "Sent verbatim to the venue")
        @NotBlank @Size(max = 500) String reason) {
}
