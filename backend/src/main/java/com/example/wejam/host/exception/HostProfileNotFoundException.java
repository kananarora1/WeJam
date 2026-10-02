package com.example.wejam.host.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class HostProfileNotFoundException extends ErrorResponseException {

    public HostProfileNotFoundException() {
        super(HttpStatus.NOT_FOUND,
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Host profile not found"),
                null);
    }
}
