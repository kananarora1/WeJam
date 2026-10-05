package com.example.wejam.verification.dto;

import com.example.wejam.venue.dto.VenueResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Everything the admin needs to decide one venue. Owner phone is for the admin only. */
public record VenueReviewResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) VenueResponse venue,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) String ownerName,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) String ownerPhone,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) VerificationChecks checks,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Uploaded documents with 5-minute download links")
        List<DocumentResponse> documents) {
}
