package com.example.wejam.auth.firebase;

public interface FirebaseTokenVerifier {

    /** @throws InvalidFirebaseTokenException if the token is invalid, expired or not for our project */
    FirebaseIdentity verify(String idToken);
}
