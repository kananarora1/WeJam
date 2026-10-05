package com.example.wejam.common.storage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * @param endpoint       how the backend itself reaches storage (e.g. http://localhost:9000)
 * @param publicEndpoint host used when signing URLs for apps — a presigned URL's signature covers the host, so it
 *                       must be the address the phone can reach (e.g. http://192.168.1.13:9000). Defaults to endpoint.
 */
@Validated
@ConfigurationProperties("wejam.storage")
public record StorageProperties(
        @NotBlank String endpoint,
        String publicEndpoint,
        @NotBlank String region,
        @NotBlank String accessKey,
        @NotBlank String secretKey,
        @NotBlank String privateBucket,
        @NotNull Duration uploadUrlTtl,
        @NotNull Duration downloadUrlTtl) {

    public String presignEndpoint() {
        return publicEndpoint == null || publicEndpoint.isBlank() ? endpoint : publicEndpoint;
    }
}
