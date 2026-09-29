package com.example.wejam.auth;

/** The verified claims we need from a Firebase ID token. {@code phoneNumber} is E.164 and may be null. */
public record FirebaseIdentity(String uid, String phoneNumber) {
}
