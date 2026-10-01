package com.example.wejam.auth.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("wejam.firebase")
public record FirebaseProperties(@NotBlank String projectId) {
}
