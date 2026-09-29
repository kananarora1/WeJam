package com.example.wejam.auth;

import com.google.firebase.ErrorCode;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Covers our mapping around the SDK; signature/claim validation itself is the SDK's job. */
class FirebaseAdminTokenVerifierTest {

    private final FirebaseAuth firebaseAuth = mock(FirebaseAuth.class);
    private final FirebaseAdminTokenVerifier verifier = new FirebaseAdminTokenVerifier(firebaseAuth);

    @Test
    void mapsUidAndPhoneAndChecksRevocation() throws Exception {
        FirebaseToken token = token("uid-123", Map.of("phone_number", "+911234567890"));
        when(firebaseAuth.verifyIdToken("id-token", true)).thenReturn(token);

        assertThat(verifier.verify("id-token")).isEqualTo(new FirebaseIdentity("uid-123", "+911234567890"));
    }

    @Test
    void missingPhoneClaimGivesNullPhone() throws Exception {
        FirebaseToken token = token("uid-123", Map.of());
        when(firebaseAuth.verifyIdToken("id-token", true)).thenReturn(token);

        assertThat(verifier.verify("id-token").phoneNumber()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = AuthErrorCode.class,
            names = {"INVALID_ID_TOKEN", "EXPIRED_ID_TOKEN", "REVOKED_ID_TOKEN", "USER_DISABLED", "USER_NOT_FOUND"})
    void tokenOrUserProblemsAreRejectedAs401(AuthErrorCode code) throws Exception {
        when(firebaseAuth.verifyIdToken("id-token", true))
                .thenThrow(authException(ErrorCode.INVALID_ARGUMENT, code));

        assertThatThrownBy(() -> verifier.verify("id-token")).isInstanceOf(InvalidFirebaseTokenException.class);
    }

    @Test
    void malformedTokenIsRejectedAs401() throws Exception {
        when(firebaseAuth.verifyIdToken("", true)).thenThrow(new IllegalArgumentException("empty"));

        assertThatThrownBy(() -> verifier.verify("")).isInstanceOf(InvalidFirebaseTokenException.class);
    }

    @Test
    void networkFailureIs503NotUnauthorized() throws Exception {
        when(firebaseAuth.verifyIdToken("id-token", true))
                .thenThrow(authException(ErrorCode.UNAVAILABLE, null));

        assertThatThrownBy(() -> verifier.verify("id-token")).isInstanceOf(FirebaseUnavailableException.class);
    }

    @Test
    void certificateFetchFailureIs503() throws Exception {
        when(firebaseAuth.verifyIdToken("id-token", true))
                .thenThrow(authException(ErrorCode.UNKNOWN, AuthErrorCode.CERTIFICATE_FETCH_FAILED));

        assertThatThrownBy(() -> verifier.verify("id-token")).isInstanceOf(FirebaseUnavailableException.class);
    }

    private static FirebaseToken token(String uid, Map<String, Object> claims) {
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn(uid);
        when(token.getClaims()).thenReturn(claims);
        return token;
    }

    private static FirebaseAuthException authException(ErrorCode errorCode, AuthErrorCode authErrorCode) {
        return new FirebaseAuthException(errorCode, "test", new IOException("test"), null, authErrorCode);
    }
}
