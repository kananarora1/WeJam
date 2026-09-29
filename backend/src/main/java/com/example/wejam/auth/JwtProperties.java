package com.example.wejam.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties("wejam.jwt")
public record JwtProperties(
        @NotBlank String issuer,
        @NotNull Duration ttl,
        // HS256 needs a key of at least 256 bits.
        @NotBlank @Size(min = 32) String secret) {
}
