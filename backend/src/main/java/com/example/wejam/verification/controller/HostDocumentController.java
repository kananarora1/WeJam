package com.example.wejam.verification.controller;

import com.example.wejam.common.web.CurrentUser;
import com.example.wejam.verification.dto.DocumentResponse;
import com.example.wejam.verification.dto.DocumentUploadRequest;
import com.example.wejam.verification.dto.DocumentUploadResponse;
import com.example.wejam.verification.dto.HostVerificationRequest;
import com.example.wejam.verification.dto.HostVerificationResponse;
import com.example.wejam.verification.service.HostDocumentService;
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

import java.util.UUID;

/** Optional host verification: never required to host. Verified profiles get a badge venues can see. */
@Tag(name = "Hosts")
@RestController
@RequestMapping("/api/v1/me/host-profile")
class HostDocumentController {

    private final HostDocumentService documentService;

    HostDocumentController(HostDocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/verification")
    HostVerificationResponse myHostVerification(@AuthenticationPrincipal Jwt jwt) {
        return documentService.status(CurrentUser.id(jwt));
    }

    @PostMapping("/verification")
    HostVerificationResponse requestHostVerification(@AuthenticationPrincipal Jwt jwt,
                                                     @Valid @RequestBody HostVerificationRequest request) {
        return documentService.submit(CurrentUser.id(jwt), request.idType());
    }

    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.CREATED)
    DocumentUploadResponse startHostDocumentUpload(@AuthenticationPrincipal Jwt jwt,
                                                   @Valid @RequestBody DocumentUploadRequest request) {
        return documentService.startUpload(CurrentUser.id(jwt), request);
    }

    @PostMapping("/documents/{documentId}/confirm")
    DocumentResponse confirmHostDocument(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID documentId) {
        return documentService.confirm(CurrentUser.id(jwt), documentId);
    }

    @DeleteMapping("/documents/{documentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteHostDocument(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID documentId) {
        documentService.delete(CurrentUser.id(jwt), documentId);
    }
}
