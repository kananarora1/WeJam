package com.example.wejam.venue.repository;

import com.example.wejam.venue.model.PhotoStatus;
import com.example.wejam.venue.model.VenuePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VenuePhotoRepository extends JpaRepository<VenuePhoto, UUID> {

    Optional<VenuePhoto> findByIdAndVenueId(UUID id, UUID venueId);

    /** Upload order; the first is the cover. */
    List<VenuePhoto> findByVenueIdAndStatusOrderByUploadedAtAscIdAsc(UUID venueId, PhotoStatus status);

    long countByVenueIdAndStatus(UUID venueId, PhotoStatus status);

    List<VenuePhoto> findByVenueId(UUID venueId);

    /** Conditional, so two confirms of the same photo can't both count it: 0 rows = already confirmed. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE venue_photos SET status = 'UPLOADED', uploaded_at = now()
            WHERE id = :photoId AND status = 'PENDING_UPLOAD'
            """, nativeQuery = true)
    int markUploaded(UUID photoId);

    /** Uploads never confirmed, locked for the cleanup job (see DocumentCleanupJob for the pattern). */
    @Query(value = """
            SELECT * FROM venue_photos
            WHERE status = 'PENDING_UPLOAD' AND created_at < :abandonedBefore
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<VenuePhoto> lockAbandoned(Instant abandonedBefore, int limit);
}
