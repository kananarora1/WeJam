package com.example.wejam.verification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectHostVerificationRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                example = "The photo is too blurry to read the name. Please upload a clearer picture of the front.",
                description = "Sent verbatim to the host; never shown to anyone else")
        @NotBlank @Size(max = 500) String reason) {
}
