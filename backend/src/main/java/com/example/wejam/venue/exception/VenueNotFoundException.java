package com.example.wejam.venue.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

/** Also used when the venue exists but belongs to someone else, so ids can't be probed. */
public class VenueNotFoundException extends ErrorResponseException {

    public VenueNotFoundException(UUID id) {
        super(HttpStatus.NOT_FOUND,
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Venue " + id + " not found"),
                null);
    }
}
