package com.example.wejam.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/** 503 rather than 401 so the client retries instead of signing the user out. */
public class FirebaseUnavailableException extends ErrorResponseException {

    public FirebaseUnavailableException(Throwable cause) {
        super(HttpStatus.SERVICE_UNAVAILABLE,
                ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                        "Could not verify Firebase token right now, please retry"),
                cause);
    }
}
