package com.example.wejam.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Pass nextBefore + nextBeforeId back to get the following page; both null on the last page. */
public record AdminActionPage(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<AdminActionResponse> items,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) Instant nextBefore,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) UUID nextBeforeId) {
}
