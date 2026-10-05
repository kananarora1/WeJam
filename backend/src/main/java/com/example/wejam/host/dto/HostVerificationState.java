package com.example.wejam.host.dto;

import com.example.wejam.host.model.HostVerificationStatus;
import com.example.wejam.host.model.IdType;

import java.time.Instant;
import java.util.UUID;

/** A host profile's verification fields, for the verification module. Not an API response. */
public record HostVerificationState(UUID profileId, UUID ownerId, HostVerificationStatus status, IdType idType,
                                    String rejectionReason, Instant requestedAt) {
}
