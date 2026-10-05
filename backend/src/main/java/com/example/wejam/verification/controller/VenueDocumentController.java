package com.example.wejam.verification.controller;

import com.example.wejam.common.web.CurrentUser;
import com.example.wejam.verification.dto.DocumentResponse;
import com.example.wejam.verification.dto.DocumentUploadRequest;
import com.example.wejam.verification.dto.DocumentUploadResponse;
import com.example.wejam.verification.service.VenueDocumentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Venues")
@RestController
@RequestMapping("/api/v1/venues/{venueId}/documents")
class VenueDocumentController {

    private final VenueDocumentService documentService;

    VenueDocumentController(VenueDocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    DocumentUploadResponse startVenueDocumentUpload(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID venueId,
                                                    @Valid @RequestBody DocumentUploadRequest request) {
        return documentService.startUpload(CurrentUser.id(jwt), venueId, request);
    }

    @PostMapping("/{documentId}/confirm")
    DocumentResponse confirmVenueDocument(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID venueId,
                                          @PathVariable UUID documentId) {
        return documentService.confirm(CurrentUser.id(jwt), venueId, documentId);
    }

    @GetMapping
    List<DocumentResponse> venueDocuments(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID venueId) {
        return documentService.list(CurrentUser.id(jwt), venueId);
    }

    @DeleteMapping("/{documentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteVenueDocument(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID venueId,
                             @PathVariable UUID documentId) {
        documentService.delete(CurrentUser.id(jwt), venueId, documentId);
    }
}
