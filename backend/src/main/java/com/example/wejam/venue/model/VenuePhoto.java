package com.example.wejam.venue.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/** A public venue photo. Status only moves PENDING_UPLOAD → UPLOADED, through a conditional update. */
@Entity
@Table(name = "venue_photos")
public class VenuePhoto {

    /** Assigned by us (not generated) because the storage key is derived from it before the insert. */
    @Id
    private UUID id;

    @Column(name = "venue_id", nullable = false, updatable = false)
    private UUID venueId;

    @Column(name = "storage_key", nullable = false, updatable = false)
    private String storageKey;

    @Column(name = "content_type", nullable = false, updatable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false, updatable = false)
    private long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private PhotoStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "uploaded_at", insertable = false, updatable = false)
    private Instant uploadedAt;

    protected VenuePhoto() {
    }

    public VenuePhoto(UUID id, UUID venueId, String storageKey, String contentType, long sizeBytes) {
        this.id = id;
        this.venueId = venueId;
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.status = PhotoStatus.PENDING_UPLOAD;
    }

    public UUID getId() {
        return id;
    }

    public UUID getVenueId() {
        return venueId;
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

    public PhotoStatus getStatus() {
        return status;
    }
}
