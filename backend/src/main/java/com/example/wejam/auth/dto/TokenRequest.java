package com.example.wejam.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(@NotBlank String firebaseIdToken) {
}
