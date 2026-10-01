package com.example.wejam.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class InvalidFirebaseTokenException extends ErrorResponseException {

    public InvalidFirebaseTokenException() {
        this(null);
    }

    public InvalidFirebaseTokenException(Throwable cause) {
        super(HttpStatus.UNAUTHORIZED,
                ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Firebase ID token is invalid or expired"),
                cause);
    }
}
