package com.example.wejam.host.model;

/** Optional, never required. Only {@link #VERIFIED} is ever visible to others (as a badge). */
public enum HostVerificationStatus {
    NOT_REQUESTED,
    PENDING,
    VERIFIED,
    REJECTED;

    /** The host can (re)submit from here; while PENDING or VERIFIED there's nothing to ask for. */
    public boolean canRequest() {
        return this == NOT_REQUESTED || this == REJECTED;
    }
}
