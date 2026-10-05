package com.example.wejam.verification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Automated checks shown next to each item, so the admin only judges what needs a human. */
public record VerificationChecks(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "14 digits, licence type 1/2, state code 01–38 (offline heuristic, not the registry)")
        boolean fssaiStructureLooksValid,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Other venues using the same FSSAI number")
        long otherVenuesWithSameFssai) {
}
