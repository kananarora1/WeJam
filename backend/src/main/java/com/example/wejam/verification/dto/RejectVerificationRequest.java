package com.example.wejam.verification.dto;

import com.example.wejam.venue.model.VerificationIssue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record RejectVerificationRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                example = "The FSSAI number doesn't match the registry. Please check the 14 digits on your license.",
                description = "Sent verbatim to the venue")
        @NotBlank @Size(max = 500) String reason,
        @Schema(nullable = true, description = "Items the venue must fix; shown flagged on their side")
        Set<VerificationIssue> issues) {

    public Set<VerificationIssue> issuesOrEmpty() {
        return issues == null ? Set.of() : issues;
    }
}
