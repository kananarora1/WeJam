package com.example.wejam.venue.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.Map;

public class VenuePhotoException extends ErrorResponseException {

    private VenuePhotoException(HttpStatus status, ProblemDetail problem) {
        super(status, problem, null);
    }

    private static VenuePhotoException of(HttpStatus status, String detail) {
        return new VenuePhotoException(status, ProblemDetail.forStatusAndDetail(status, detail));
    }

    public static VenuePhotoException notFound() {
        return of(HttpStatus.NOT_FOUND, "Photo not found");
    }

    public static VenuePhotoException tooMany(int max) {
        return of(HttpStatus.CONFLICT, "A venue can have up to " + max + " photos; delete one first");
    }

    public static VenuePhotoException notUploadedYet() {
        return of(HttpStatus.CONFLICT, "Upload the file to the URL first, then confirm");
    }

    public static VenuePhotoException uploadMismatch() {
        return of(HttpStatus.CONFLICT, "The uploaded file doesn't match what was declared. Start the upload again.");
    }

    public static VenuePhotoException unsupportedContentType() {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request content.");
        problem.setProperty("errors", Map.of("contentType", "must be image/jpeg, image/png or image/webp"));
        return new VenuePhotoException(HttpStatus.BAD_REQUEST, problem);
    }
}
