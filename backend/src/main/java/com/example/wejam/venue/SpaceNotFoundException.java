package com.example.wejam.venue;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

public class SpaceNotFoundException extends ErrorResponseException {

    public SpaceNotFoundException(UUID id) {
        super(HttpStatus.NOT_FOUND,
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Space " + id + " not found"),
                null);
    }
}
