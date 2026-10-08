package com.example.wejam.verification.service;

import com.example.wejam.venue.service.VenueSubmissionRequirement;
import com.example.wejam.verification.model.DocumentOwnerType;
import com.example.wejam.verification.model.DocumentType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/** With no registry lookup, the certificate is the reviewer's only evidence of the license name and expiry. */
@Component
class FssaiCertificateRequirement implements VenueSubmissionRequirement {

    private final VerificationDocumentStore store;

    FssaiCertificateRequirement(VerificationDocumentStore store) {
        this.store = store;
    }

    @Override
    public Map<String, String> missing(UUID venueId) {
        return store.hasUploaded(DocumentOwnerType.VENUE, venueId, DocumentType.FSSAI_CERTIFICATE)
                ? Map.of()
                : Map.of("fssaiCertificate", "upload a photo or PDF of your FSSAI certificate");
    }
}
