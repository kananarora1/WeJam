package com.example.wejam.host.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.Map;

/** Cross-field rules Bean Validation can't express on the record; same shape as field validation errors. */
public class InvalidHostProfileException extends ErrorResponseException {

    public InvalidHostProfileException(Map<String, String> errors) {
        super(HttpStatus.BAD_REQUEST, problem(errors), null);
    }

    private static ProblemDetail problem(Map<String, String> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request content.");
        problem.setProperty("errors", errors);
        return problem;
    }
}
