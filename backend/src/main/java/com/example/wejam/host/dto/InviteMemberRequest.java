package com.example.wejam.host.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record InviteMemberRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "+911234567890")
        @NotBlank @Pattern(regexp = "\\+[1-9]\\d{7,14}", message = "must be a phone number in international format")
        String phoneNumber) {
}
