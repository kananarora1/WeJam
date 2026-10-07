package com.example.wejam.venue.service;

import com.example.wejam.common.storage.Bucket;
import com.example.wejam.common.storage.ObjectStorage;
import com.example.wejam.common.storage.PresignedUpload;
import com.example.wejam.common.storage.StoredObject;
import com.example.wejam.venue.dto.PhotoUploadRequest;
import com.example.wejam.venue.dto.PhotoUploadResponse;
import com.example.wejam.venue.dto.VenuePhotoDto;
import com.example.wejam.venue.exception.VenueNotFoundException;
import com.example.wejam.venue.exception.VenuePhotoException;
import com.example.wejam.venue.model.PhotoStatus;
import com.example.wejam.venue.model.VenuePhoto;
import com.example.wejam.venue.repository.VenuePhotoRepository;
import com.example.wejam.venue.repository.VenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Public venue photos (design 7.1), same two-phase upload as documents but to the public bucket. The owner can
 * change them in any state; they never affect verification. The first uploaded photo is the cover.
 */
@Service
@Transactional
public class VenuePhotoService {

    public static final int MAX_PHOTOS = 10;
    static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final VenuePhotoRepository photos;
    private final VenueRepository venues;
    private final ObjectStorage storage;

    public VenuePhotoService(VenuePhotoRepository photos, VenueRepository venues, ObjectStorage storage) {
        this.photos = photos;
        this.venues = venues;
        this.storage = storage;
    }

    public PhotoUploadResponse startUpload(UUID ownerId, UUID venueId, PhotoUploadRequest request) {
        requireOwned(ownerId, venueId);
        if (!ALLOWED_CONTENT_TYPES.contains(request.contentType())) {
            throw VenuePhotoException.unsupportedContentType();
        }
        // A courtesy check so nobody uploads an 11th file for nothing; confirm() is the real guard.
        if (photos.countByVenueIdAndStatus(venueId, PhotoStatus.UPLOADED) >= MAX_PHOTOS) {
            throw VenuePhotoException.tooMany(MAX_PHOTOS);
        }
        UUID id = UUID.randomUUID();
        String key = "venues/" + venueId + "/photos/" + id;
        photos.save(new VenuePhoto(id, venueId, key, request.contentType(), request.sizeBytes()));
        PresignedUpload upload = storage.presignUpload(Bucket.PUBLIC, key, request.contentType(), request.sizeBytes());
        return new PhotoUploadResponse(id, upload.url(), "PUT", upload.headers(), upload.expiresAt());
    }

    public VenuePhotoDto confirm(UUID ownerId, UUID venueId, UUID photoId) {
        requireOwned(ownerId, venueId);
        VenuePhoto photo = photos.findByIdAndVenueId(photoId, venueId).orElseThrow(VenuePhotoException::notFound);
        if (photo.getStatus() == PhotoStatus.UPLOADED) {
            return toDto(photo); // confirming twice is harmless
        }
        StoredObject stored = storage.head(Bucket.PUBLIC, photo.getStorageKey())
                .orElseThrow(VenuePhotoException::notUploadedYet);
        if (stored.sizeBytes() != photo.getSizeBytes() || !photo.getContentType().equals(stored.contentType())) {
            throw VenuePhotoException.uploadMismatch(); // can't normally happen: both are signed
        }

        // Both writes are conditional and share this transaction: a concurrent confirm of the same photo
        // finds it already UPLOADED, and if the venue is full the exception undoes the status change too.
        if (photos.markUploaded(photoId) == 0) {
            return toDto(photo);
        }
        if (venues.reservePhotoSlot(venueId, MAX_PHOTOS) == 0) {
            throw VenuePhotoException.tooMany(MAX_PHOTOS);
        }
        return toDto(photo);
    }

    public void delete(UUID ownerId, UUID venueId, UUID photoId) {
        requireOwned(ownerId, venueId);
        VenuePhoto photo = photos.findByIdAndVenueId(photoId, venueId).orElseThrow(VenuePhotoException::notFound);
        if (photo.getStatus() == PhotoStatus.UPLOADED) {
            venues.releasePhotoSlot(venueId);
        }
        photos.delete(photo);
        storage.deleteAfterCommit(Bucket.PUBLIC, photo.getStorageKey());
    }

    /** Upload order; the first is the cover. */
    @Transactional(readOnly = true)
    public List<VenuePhotoDto> photosOf(UUID venueId) {
        return photos.findByVenueIdAndStatusOrderByUploadedAtAscIdAsc(venueId, PhotoStatus.UPLOADED).stream()
                .map(this::toDto).toList();
    }

    /** Part of deleting a venue: rows now, files after commit. */
    void deleteAllOf(UUID venueId) {
        List<VenuePhoto> all = photos.findByVenueId(venueId);
        photos.deleteAll(all);
        all.forEach(p -> storage.deleteAfterCommit(Bucket.PUBLIC, p.getStorageKey()));
    }

    private void requireOwned(UUID ownerId, UUID venueId) {
        if (!venues.existsByIdAndOwnerId(venueId, ownerId)) {
            throw new VenueNotFoundException(venueId);
        }
    }

    private VenuePhotoDto toDto(VenuePhoto photo) {
        return new VenuePhotoDto(photo.getId(), storage.publicUrl(photo.getStorageKey()));
    }
}
