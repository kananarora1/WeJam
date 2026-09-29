package com.example.wejam.auth;

public enum Role {
    USER,
    HOST,
    VENUE_ADMIN;

    /** USER is granted to everyone at first login; only the others can be requested. */
    public boolean isSelfAssignable() {
        return this != USER;
    }
}
