package com.example.wejam.host.dto;

import com.example.wejam.host.model.GroupKind;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/** Invitee-side view: which group invited me, and who runs it. */
public record HostInviteResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID hostProfileId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String groupName,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) GroupKind groupKind,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) String invitedByName,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant invitedAt) {
}
