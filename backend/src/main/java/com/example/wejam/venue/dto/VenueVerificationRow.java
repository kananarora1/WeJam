package com.example.wejam.venue.dto;

import com.example.wejam.venue.model.VerificationStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** One row of the platform admin's review queue; built directly by the repository query. */
public record VenueVerificationRow(
        UUID venueId,
        String name,
        String city,
        UUID ownerId,
        String fssaiNumber,
        LocalDate fssaiValidUntil,
        int photoCount,
        VerificationStatus status,
        Instant requestedAt,
        long otherVenuesWithSameFssai) {
}
