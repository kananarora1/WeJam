package com.example.wejam.venue.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/** A verification transition that isn't allowed from the venue's current state. */
public class VerificationStateException extends ErrorResponseException {

    private VerificationStateException(String detail) {
        super(HttpStatus.CONFLICT, ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail), null);
    }

    public static VerificationStateException notPending() {
        return new VerificationStateException("This venue is not waiting for review (it may already have been decided)");
    }

    public static VerificationStateException notRejected() {
        return new VerificationStateException("Only a venue that was not verified can be resubmitted");
    }

    public static VerificationStateException fssaiAlreadyVerified() {
        return new VerificationStateException("This FSSAI number is already verified for another venue");
    }
}
