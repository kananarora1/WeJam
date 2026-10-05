package com.example.wejam.common.storage;

import java.time.Instant;
import java.util.Map;

/** The app must PUT to {@code url} sending exactly {@code headers}; anything else fails the signature check. */
public record PresignedUpload(String url, Map<String, String> headers, Instant expiresAt) {
}
