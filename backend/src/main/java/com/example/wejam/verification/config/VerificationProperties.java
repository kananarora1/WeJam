package com.example.wejam.verification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * @param idRetention        how long a host's ID is kept after review (or after upload, if never submitted)
 * @param abandonedUploadAge an upload started but not confirmed within this long is cleaned up
 */
@ConfigurationProperties("wejam.verification")
public record VerificationProperties(Duration idRetention, Duration abandonedUploadAge) {
}
