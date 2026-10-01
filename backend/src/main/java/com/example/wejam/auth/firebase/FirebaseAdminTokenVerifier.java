package com.example.wejam.auth.firebase;

import com.example.wejam.auth.exception.FirebaseUnavailableException;
import com.example.wejam.auth.exception.InvalidFirebaseTokenException;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Set;

public class FirebaseAdminTokenVerifier implements FirebaseTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(FirebaseAdminTokenVerifier.class);

    /** Failures caused by the token or the user. Anything else (network, Google outage) is not the client's fault. */
    private static final Set<AuthErrorCode> REJECTED_TOKEN_CODES = EnumSet.of(
            AuthErrorCode.INVALID_ID_TOKEN,
            AuthErrorCode.EXPIRED_ID_TOKEN,
            AuthErrorCode.REVOKED_ID_TOKEN,
            AuthErrorCode.USER_DISABLED,
            AuthErrorCode.USER_NOT_FOUND,
            AuthErrorCode.TENANT_ID_MISMATCH);

    private final FirebaseAuth firebaseAuth;

    public FirebaseAdminTokenVerifier(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public FirebaseIdentity verify(String idToken) {
        try {
            // checkRevoked=true also calls Firebase to reject disabled users and revoked sessions.
            FirebaseToken token = firebaseAuth.verifyIdToken(idToken, true);
            return new FirebaseIdentity(token.getUid(), (String) token.getClaims().get("phone_number"));
        } catch (IllegalArgumentException e) {
            throw new InvalidFirebaseTokenException(e);
        } catch (FirebaseAuthException e) {
            if (REJECTED_TOKEN_CODES.contains(e.getAuthErrorCode())) {
                throw new InvalidFirebaseTokenException(e);
            }
            log.warn("Firebase token verification unavailable: {} / {}", e.getErrorCode(), e.getAuthErrorCode(), e);
            throw new FirebaseUnavailableException(e);
        }
    }
}
