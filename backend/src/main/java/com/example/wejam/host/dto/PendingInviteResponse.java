package com.example.wejam.host.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/** Owner-side view of an invite: the invitee's name stays hidden until they accept. */
public record PendingInviteResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID userId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "+91 12•••••890") String maskedPhone,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant invitedAt) {
}
