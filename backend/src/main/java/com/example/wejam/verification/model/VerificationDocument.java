package com.example.wejam.verification.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "verification_documents")
public class VerificationDocument {

    /** Assigned by us (not generated) because the storage key is derived from it before the insert. */
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, updatable = false)
    private DocumentOwnerType ownerType;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false, updatable = false)
    private DocumentType docType;

    @Column(name = "storage_key", nullable = false, updatable = false)
    private String storageKey;

    @Column(name = "content_type", nullable = false, updatable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false, updatable = false)
    private long sizeBytes;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "uploaded_at")
    private Instant uploadedAt;

    protected VerificationDocument() {
    }

    public VerificationDocument(UUID id, DocumentOwnerType ownerType, UUID ownerId, DocumentType docType,
                                String storageKey, String contentType, long sizeBytes) {
        this.id = id;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.docType = docType;
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.status = DocumentStatus.PENDING_UPLOAD;
    }

    public void markUploaded() {
        this.status = DocumentStatus.UPLOADED;
        this.uploadedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public DocumentOwnerType getOwnerType() {
        return ownerType;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public DocumentType getDocType() {
        return docType;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }
}
