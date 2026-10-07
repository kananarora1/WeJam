package com.example.wejam.common.storage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * @param endpoint           how the backend itself reaches storage (e.g. http://localhost:9000)
 * @param publicEndpoint     host used when signing URLs for apps — a presigned URL's signature covers the host, so
 *                           it must be the address the phone can reach (e.g. http://192.168.1.9:9000). Defaults to
 *                           endpoint.
 * @param publicBaseUrl      where public files are served from, e.g. a CDN. Defaults to the public bucket on
 *                           {@link #presignEndpoint()}.
 * @param abandonedUploadAge an upload started but not confirmed within this long is cleaned up
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
        @NotBlank String publicBucket,
        String publicBaseUrl,
        @NotNull Duration uploadUrlTtl,
        @NotNull Duration downloadUrlTtl,
        @NotNull Duration abandonedUploadAge) {

    public String presignEndpoint() {
        return publicEndpoint == null || publicEndpoint.isBlank() ? endpoint : publicEndpoint;
    }

    /** No trailing slash. Path-style, like every other URL we build. */
    public String publicFilesUrl() {
        String base = publicBaseUrl == null || publicBaseUrl.isBlank()
                ? presignEndpoint() + "/" + publicBucket
                : publicBaseUrl;
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    public String bucketName(Bucket bucket) {
        return switch (bucket) {
            case PRIVATE -> privateBucket;
            case PUBLIC -> publicBucket;
        };
    }
}
