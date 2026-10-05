package com.example.wejam.verification.dto;

import com.example.wejam.host.model.IdType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Asks for the verified badge. Upload ID_FRONT (and optionally ID_BACK) first. */
public record HostVerificationRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) @NotNull IdType idType) {
}
