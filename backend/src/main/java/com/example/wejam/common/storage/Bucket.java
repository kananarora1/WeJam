package com.example.wejam.common.storage;

/** Where an object lives. Private: only via short-lived presigned links. Public: readable by URL, not listable. */
public enum Bucket {
    PRIVATE,
    PUBLIC
}
