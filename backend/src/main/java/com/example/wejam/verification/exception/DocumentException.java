package com.example.wejam.verification.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.Map;

public class DocumentException extends ErrorResponseException {

    private DocumentException(HttpStatus status, String detail) {
        super(status, ProblemDetail.forStatusAndDetail(status, detail), null);
    }

    private DocumentException(HttpStatus status, String detail, Map<String, String> errors) {
        super(status, problem(status, detail, errors), null);
    }

    private static ProblemDetail problem(HttpStatus status, String detail, Map<String, String> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty("errors", errors);
        return problem;
    }

    public static DocumentException notFound() {
        return new DocumentException(HttpStatus.NOT_FOUND, "Document not found");
    }

    public static DocumentException venueAlreadyVerified() {
        return new DocumentException(HttpStatus.CONFLICT, "This venue is already verified; documents can't be changed");
    }

    public static DocumentException notUploadedYet() {
        return new DocumentException(HttpStatus.CONFLICT, "Upload the file to the URL first, then confirm");
    }

    public static DocumentException uploadMismatch() {
        return new DocumentException(HttpStatus.CONFLICT,
                "The uploaded file doesn't match what was declared. Start the upload again.");
    }

    public static DocumentException unsupportedContentType() {
        return new DocumentException(HttpStatus.BAD_REQUEST, "Invalid request content.",
                Map.of("contentType", "must be image/jpeg, image/png or application/pdf"));
    }

    public static DocumentException hostAlreadyVerified() {
        return new DocumentException(HttpStatus.CONFLICT, "You're already verified; your ID can't be changed");
    }

    public static DocumentException underReview() {
        return new DocumentException(HttpStatus.CONFLICT,
                "Your ID is being reviewed; it can't be changed until there's a decision");
    }

    public static DocumentException idFrontRequired() {
        return new DocumentException(HttpStatus.CONFLICT, "Upload the front of your ID first");
    }

    /** {@code expected} completes "type …", e.g. "is not a venue document type". */
    public static DocumentException wrongDocumentType(String expected) {
        return new DocumentException(HttpStatus.BAD_REQUEST, "Invalid request content.", Map.of("type", expected));
    }
}
