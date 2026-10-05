package com.example.wejam.host.dto;

import com.example.wejam.host.model.HostType;
import com.example.wejam.host.model.HostVerificationStatus;
import com.example.wejam.host.model.IdType;

import java.time.Instant;
import java.util.UUID;

/** One line of the admin's host verification queue (JPQL projection). */
public record HostVerificationRow(UUID profileId, HostType type, String groupName, UUID ownerId, IdType idType,
                                  HostVerificationStatus status, Instant requestedAt) {
}
