package com.example.wejam.verification.service;

import com.example.wejam.common.storage.ObjectStorage;
import com.example.wejam.common.storage.PresignedUpload;
import com.example.wejam.common.storage.StoredObject;
import com.example.wejam.verification.dto.DocumentResponse;
import com.example.wejam.verification.dto.DocumentUploadRequest;
import com.example.wejam.verification.dto.DocumentUploadResponse;
import com.example.wejam.verification.exception.DocumentException;
import com.example.wejam.verification.model.DocumentOwnerType;
import com.example.wejam.verification.model.DocumentStatus;
import com.example.wejam.verification.model.DocumentType;
import com.example.wejam.verification.model.VerificationDocument;
import com.example.wejam.verification.repository.VerificationDocumentRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The owner-agnostic half of verification documents. Two-phase upload: we hand out a presigned PUT (bytes go
 * straight to storage), then the app confirms and we check the object really is there with the declared size
 * and type. Callers decide who may do what; everything here runs inside their transaction.
 */
@Component
class VerificationDocumentStore {

    static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "application/pdf");

    private final VerificationDocumentRepository documents;
    private final ObjectStorage storage;

    VerificationDocumentStore(VerificationDocumentRepository documents, ObjectStorage storage) {
        this.documents = documents;
        this.storage = storage;
    }

    DocumentUploadResponse startUpload(DocumentOwnerType ownerType, UUID ownerId, DocumentUploadRequest request) {
        if (!ALLOWED_CONTENT_TYPES.contains(request.contentType())) {
            throw DocumentException.unsupportedContentType();
        }
        UUID id = UUID.randomUUID();
        String key = "verification/" + folder(ownerType) + "/" + ownerId + "/" + id;
        documents.save(new VerificationDocument(id, ownerType, ownerId, request.type(), key,
                request.contentType(), request.sizeBytes()));
        PresignedUpload upload = storage.presignUpload(key, request.contentType(), request.sizeBytes());
        return new DocumentUploadResponse(id, upload.url(), "PUT", upload.headers(), upload.expiresAt());
    }

    /** Marks the document uploaded (idempotent) and replaces the previous one of the same type. */
    VerificationDocument confirm(DocumentOwnerType ownerType, UUID ownerId, UUID documentId) {
        VerificationDocument document = owned(ownerType, ownerId, documentId);
        if (document.getStatus() == DocumentStatus.UPLOADED) {
            return document; // confirming twice is harmless
        }
        StoredObject stored = storage.head(document.getStorageKey()).orElseThrow(DocumentException::notUploadedYet);
        if (stored.sizeBytes() != document.getSizeBytes() || !document.getContentType().equals(stored.contentType())) {
            // Can't normally happen: size and type are part of the upload signature.
            throw DocumentException.uploadMismatch();
        }

        documents.findByOwnerTypeAndOwnerIdAndDocTypeAndStatus(ownerType, ownerId, document.getDocType(),
                DocumentStatus.UPLOADED).ifPresent(previous -> {
                    documents.delete(previous);
                    // Hibernate flushes UPDATEs before DELETEs; without this flush, marking the new one UPLOADED
                    // would hit the one-current-document-per-type unique index while the old row still exists.
                    documents.flush();
                    storage.deleteAfterCommit(previous.getStorageKey());
                });
        document.markUploaded();
        return document;
    }

    List<DocumentResponse> uploaded(DocumentOwnerType ownerType, UUID ownerId) {
        return documents.findByOwnerTypeAndOwnerIdAndStatusOrderByDocType(ownerType, ownerId, DocumentStatus.UPLOADED)
                .stream().map(this::toResponse).toList();
    }

    boolean hasUploaded(DocumentOwnerType ownerType, UUID ownerId, DocumentType type) {
        return documents.existsByOwnerTypeAndOwnerIdAndDocTypeAndStatus(ownerType, ownerId, type,
                DocumentStatus.UPLOADED);
    }

    void delete(DocumentOwnerType ownerType, UUID ownerId, UUID documentId) {
        VerificationDocument document = owned(ownerType, ownerId, documentId);
        documents.delete(document);
        storage.deleteAfterCommit(document.getStorageKey());
    }

    /** Rows now, files after commit. */
    void deleteAll(DocumentOwnerType ownerType, UUID ownerId) {
        List<VerificationDocument> all = documents.findByOwnerTypeAndOwnerId(ownerType, ownerId);
        documents.deleteAll(all);
        all.forEach(d -> storage.deleteAfterCommit(d.getStorageKey()));
    }

    /** {@code at} = null keeps the uploaded documents indefinitely. */
    void setDeleteAfter(DocumentOwnerType ownerType, UUID ownerId, Instant at) {
        documents.setDeleteAfter(ownerType, ownerId, at);
    }

    DocumentResponse toResponse(VerificationDocument d) {
        return new DocumentResponse(d.getId(), d.getDocType(), d.getContentType(), d.getSizeBytes(), d.getUploadedAt(),
                storage.presignDownload(d.getStorageKey()), storage.downloadUrlExpiry(), d.getDeleteAfter());
    }

    private VerificationDocument owned(DocumentOwnerType ownerType, UUID ownerId, UUID documentId) {
        return documents.findByIdAndOwnerTypeAndOwnerId(documentId, ownerType, ownerId)
                .orElseThrow(DocumentException::notFound);
    }

    private static String folder(DocumentOwnerType ownerType) {
        return switch (ownerType) {
            case VENUE -> "venues";
            case HOST_PROFILE -> "hosts";
        };
    }
}
