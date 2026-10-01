package com.example.wejam.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

public class UserNotFoundException extends ErrorResponseException {

    public UserNotFoundException(UUID id) {
        super(HttpStatus.NOT_FOUND,
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "User " + id + " not found"),
                null);
    }
}
