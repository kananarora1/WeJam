package com.example.wejam.common.storage;

/** What storage says about an uploaded object (from a HEAD request). */
public record StoredObject(long sizeBytes, String contentType) {
}
