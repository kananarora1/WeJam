package com.example.wejam.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record TokenRequest(@Schema(requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank String firebaseIdToken) {
}
