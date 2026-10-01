package com.example.wejam.auth.exception;

import com.example.wejam.auth.model.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class RoleNotSelfAssignableException extends ErrorResponseException {

    public RoleNotSelfAssignableException(Role role) {
        super(HttpStatus.BAD_REQUEST,
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Role " + role + " cannot be self-assigned"),
                null);
    }
}
