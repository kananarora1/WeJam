package com.example.wejam.venue.dto;

import com.example.wejam.venue.model.HostingMode;
import com.example.wejam.venue.model.Venue;
import com.example.wejam.venue.model.VerificationIssue;
import com.example.wejam.venue.model.VerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

public record VenueResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String name,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) String description,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String addressLine,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String city,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) double latitude,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) double longitude,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String fssaiNumber,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) HostingMode hostingMode,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) VerificationStatus verificationStatus,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) String rejectionReason,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "What to fix after a rejection; empty otherwise")
        List<VerificationIssue> verificationIssues,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<SpaceResponse> spaces) {

    public static VenueResponse from(Venue venue, List<SpaceResponse> spaces) {
        return new VenueResponse(venue.getId(), venue.getName(), venue.getDescription(), venue.getAddressLine(),
                venue.getCity(), venue.getLatitude(), venue.getLongitude(), venue.getFssaiNumber(),
                venue.getHostingMode(), venue.getVerificationStatus(), venue.getRejectionReason(), venue.getVerificationIssues(), spaces);
    }
}
