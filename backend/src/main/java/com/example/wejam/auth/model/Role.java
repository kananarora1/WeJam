package com.example.wejam.auth.model;

public enum Role {
    USER,
    HOST,
    VENUE_ADMIN,
    /** Only ever granted from the configured allow-list at login (PlatformAdminProperties). */
    PLATFORM_ADMIN;

    /** An explicit allow-list, so a new role is never self-assignable by accident. */
    public boolean isSelfAssignable() {
        return this == HOST || this == VENUE_ADMIN;
    }
}
