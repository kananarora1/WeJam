package com.example.wejam.admin.dto;

import com.example.wejam.admin.model.AdminActionType;
import com.example.wejam.admin.model.AdminTargetType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record AdminActionResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID adminId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) String adminName,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) AdminActionType action,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) AdminTargetType targetType,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID targetId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) String reason,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt) {
}
