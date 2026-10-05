package com.example.wejam.verification.service;

import com.example.wejam.common.storage.ObjectStorage;
import com.example.wejam.common.storage.PresignedUpload;
import com.example.wejam.common.storage.StoredObject;
import com.example.wejam.venue.event.VenueDeletedEvent;
import com.example.wejam.venue.model.VerificationStatus;
import com.example.wejam.venue.service.VenueService;
import com.example.wejam.verification.dto.DocumentResponse;
import com.example.wejam.verification.dto.DocumentUploadRequest;
import com.example.wejam.verification.dto.DocumentUploadResponse;
import com.example.wejam.verification.exception.DocumentException;
import com.example.wejam.verification.model.DocumentOwnerType;
import com.example.wejam.verification.model.DocumentStatus;
import com.example.wejam.verification.model.DocumentType;
import com.example.wejam.verification.model.VerificationDocument;
import com.example.wejam.verification.repository.VerificationDocumentRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Venue verification documents. Two-phase upload: we hand out a presigned PUT (bytes go straight to storage),
 * then the app confirms and we check the object really is there with the declared size and type.
 */
@Service
@Transactional
public class VenueDocumentService {

    static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "application/pdf");
    private static final Set<DocumentType> VENUE_TYPES =
            EnumSet.of(DocumentType.FSSAI_CERTIFICATE, DocumentType.LEASE_AGREEMENT);

    private final VerificationDocumentRepository documents;
    private final VenueService venueService;
    private final ObjectStorage storage;

    public VenueDocumentService(VerificationDocumentRepository documents, VenueService venueService,
                                ObjectStorage storage) {
        this.documents = documents;
        this.venueService = venueService;
        this.storage = storage;
    }

    public DocumentUploadResponse startUpload(UUID ownerId, UUID venueId, DocumentUploadRequest request) {
        requireEditable(ownerId, venueId);
        if (!VENUE_TYPES.contains(request.type())) {
            throw DocumentException.notAVenueDocument();
        }
        if (!ALLOWED_CONTENT_TYPES.contains(request.contentType())) {
            throw DocumentException.unsupportedContentType();
        }
        UUID id = UUID.randomUUID();
        String key = "verification/venues/" + venueId + "/" + id;
        documents.save(new VerificationDocument(id, DocumentOwnerType.VENUE, venueId, request.type(), key,
                request.contentType(), request.sizeBytes()));
        PresignedUpload upload = storage.presignUpload(key, request.contentType(), request.sizeBytes());
        return new DocumentUploadResponse(id, upload.url(), "PUT", upload.headers(), upload.expiresAt());
    }

    public DocumentResponse confirm(UUID ownerId, UUID venueId, UUID documentId) {
        requireEditable(ownerId, venueId);
        VerificationDocument document = venueDocument(venueId, documentId);
        if (document.getStatus() == DocumentStatus.UPLOADED) {
            return toResponse(document); // confirming twice is harmless
        }
        StoredObject stored = storage.head(document.getStorageKey()).orElseThrow(DocumentException::notUploadedYet);
        if (stored.sizeBytes() != document.getSizeBytes() || !document.getContentType().equals(stored.contentType())) {
            // Can't normally happen: size and type are part of the upload signature.
            throw DocumentException.uploadMismatch();
        }

        documents.findByOwnerTypeAndOwnerIdAndDocTypeAndStatus(DocumentOwnerType.VENUE, venueId,
                document.getDocType(), DocumentStatus.UPLOADED).ifPresent(previous -> {
                    documents.delete(previous);
                    // Hibernate flushes UPDATEs before DELETEs; without this flush, marking the new one UPLOADED
                    // would hit the one-current-document-per-type unique index while the old row still exists.
                    documents.flush();
                    storage.deleteAfterCommit(previous.getStorageKey());
                });
        document.markUploaded();
        return toResponse(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> list(UUID ownerId, UUID venueId) {
        venueService.ownedVenueStatus(ownerId, venueId);
        return uploadedDocuments(venueId);
    }

    public void delete(UUID ownerId, UUID venueId, UUID documentId) {
        requireEditable(ownerId, venueId);
        VerificationDocument document = venueDocument(venueId, documentId);
        documents.delete(document);
        storage.deleteAfterCommit(document.getStorageKey());
    }

    /** For the platform admin's review; the caller checks the role. */
    @Transactional(readOnly = true)
    List<DocumentResponse> uploadedDocuments(UUID venueId) {
        return documents.findByOwnerTypeAndOwnerIdAndStatusOrderByDocType(DocumentOwnerType.VENUE, venueId,
                DocumentStatus.UPLOADED).stream().map(this::toResponse).toList();
    }

    /** Runs inside the venue's delete transaction; objects go only after it commits. */
    @EventListener
    public void onVenueDeleted(VenueDeletedEvent event) {
        List<VerificationDocument> owned = documents.findByOwnerTypeAndOwnerId(DocumentOwnerType.VENUE, event.venueId());
        documents.deleteAll(owned);
        owned.forEach(d -> storage.deleteAfterCommit(d.getStorageKey()));
    }

    private void requireEditable(UUID ownerId, UUID venueId) {
        if (venueService.ownedVenueStatus(ownerId, venueId) == VerificationStatus.VERIFIED) {
            throw DocumentException.venueAlreadyVerified();
        }
    }

    private VerificationDocument venueDocument(UUID venueId, UUID documentId) {
        return documents.findByIdAndOwnerTypeAndOwnerId(documentId, DocumentOwnerType.VENUE, venueId)
                .orElseThrow(DocumentException::notFound);
    }

    private DocumentResponse toResponse(VerificationDocument d) {
        return new DocumentResponse(d.getId(), d.getDocType(), d.getContentType(), d.getSizeBytes(), d.getUploadedAt(),
                storage.presignDownload(d.getStorageKey()), storage.downloadUrlExpiry());
    }
}
