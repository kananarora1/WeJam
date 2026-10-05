package com.example.wejam.auth.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("wejam.firebase")
public record FirebaseProperties(
        @NotBlank String projectId,
        /** Service-account JSON path. Blank = Google's standard lookup (GOOGLE_APPLICATION_CREDENTIALS env var). */
        String credentialsFile) {
}
