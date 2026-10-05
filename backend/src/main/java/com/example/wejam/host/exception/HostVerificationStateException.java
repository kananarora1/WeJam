package com.example.wejam.host.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class HostVerificationStateException extends ErrorResponseException {

    private HostVerificationStateException(String detail) {
        super(HttpStatus.CONFLICT, ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail), null);
    }

    public static HostVerificationStateException alreadyRequested() {
        return new HostVerificationStateException("Your ID is already being reviewed, or you're already verified");
    }

    public static HostVerificationStateException notPending() {
        return new HostVerificationStateException("This host isn't waiting for review");
    }
}
