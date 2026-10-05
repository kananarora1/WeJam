package com.example.wejam.verification.service;

import com.example.wejam.venue.event.VenueDeletedEvent;
import com.example.wejam.venue.model.VerificationStatus;
import com.example.wejam.venue.service.VenueService;
import com.example.wejam.verification.dto.DocumentResponse;
import com.example.wejam.verification.dto.DocumentUploadRequest;
import com.example.wejam.verification.dto.DocumentUploadResponse;
import com.example.wejam.verification.exception.DocumentException;
import com.example.wejam.verification.model.DocumentOwnerType;
import com.example.wejam.verification.model.DocumentType;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Venue verification documents: the owner, only while the venue isn't verified. Kept until the venue is deleted. */
@Service
@Transactional
public class VenueDocumentService {

    private static final Set<DocumentType> VENUE_TYPES =
            EnumSet.of(DocumentType.FSSAI_CERTIFICATE, DocumentType.LEASE_AGREEMENT);

    private final VerificationDocumentStore store;
    private final VenueService venueService;

    public VenueDocumentService(VerificationDocumentStore store, VenueService venueService) {
        this.store = store;
        this.venueService = venueService;
    }

    public DocumentUploadResponse startUpload(UUID ownerId, UUID venueId, DocumentUploadRequest request) {
        requireEditable(ownerId, venueId);
        if (!VENUE_TYPES.contains(request.type())) {
            throw DocumentException.wrongDocumentType("is not a venue document type");
        }
        return store.startUpload(DocumentOwnerType.VENUE, venueId, request);
    }

    public DocumentResponse confirm(UUID ownerId, UUID venueId, UUID documentId) {
        requireEditable(ownerId, venueId);
        return store.toResponse(store.confirm(DocumentOwnerType.VENUE, venueId, documentId));
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> list(UUID ownerId, UUID venueId) {
        venueService.ownedVenueStatus(ownerId, venueId);
        return uploadedDocuments(venueId);
    }

    public void delete(UUID ownerId, UUID venueId, UUID documentId) {
        requireEditable(ownerId, venueId);
        store.delete(DocumentOwnerType.VENUE, venueId, documentId);
    }

    /** For the platform admin's review; the caller checks the role. */
    @Transactional(readOnly = true)
    List<DocumentResponse> uploadedDocuments(UUID venueId) {
        return store.uploaded(DocumentOwnerType.VENUE, venueId);
    }

    /** Runs inside the venue's delete transaction; objects go only after it commits. */
    @EventListener
    public void onVenueDeleted(VenueDeletedEvent event) {
        store.deleteAll(DocumentOwnerType.VENUE, event.venueId());
    }

    private void requireEditable(UUID ownerId, UUID venueId) {
        if (venueService.ownedVenueStatus(ownerId, venueId) == VerificationStatus.VERIFIED) {
            throw DocumentException.venueAlreadyVerified();
        }
    }
}
