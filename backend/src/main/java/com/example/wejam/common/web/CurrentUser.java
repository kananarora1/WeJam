package com.example.wejam.common.web;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/** The app JWT's subject is the user id (see auth.JwtService). */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static UUID id(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
