package com.example.wejam.venue.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.Map;

/** What's still missing before a venue can be (or stay) submitted; same shape as field validation errors. */
public class IncompleteVenueException extends ErrorResponseException {

    public IncompleteVenueException(Map<String, String> errors) {
        super(HttpStatus.BAD_REQUEST, problem(errors), null);
    }

    private static ProblemDetail problem(Map<String, String> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request content.");
        problem.setProperty("errors", errors);
        return problem;
    }
}
