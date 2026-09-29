package com.example.wejam.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Accepts tokens of the form {@code fake:<uid>} or {@code fake:<uid>:<phone>} without contacting Firebase.
 * Only for tests and local development: anyone can log in as anyone when this is active.
 */
@Component
@ConditionalOnProperty(name = "wejam.auth.fake-firebase", havingValue = "true")
class FakeFirebaseTokenVerifier implements FirebaseTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(FakeFirebaseTokenVerifier.class);
    private static final String PREFIX = "fake:";

    FakeFirebaseTokenVerifier() {
        log.warn("FAKE Firebase token verification is ENABLED — never use this outside local dev/tests");
    }

    @Override
    public FirebaseIdentity verify(String idToken) {
        if (!idToken.startsWith(PREFIX)) {
            throw new InvalidFirebaseTokenException();
        }
        String[] parts = idToken.substring(PREFIX.length()).split(":", 2);
        if (parts[0].isBlank()) {
            throw new InvalidFirebaseTokenException();
        }
        return new FirebaseIdentity(parts[0], parts.length > 1 ? parts[1] : null);
    }
}
