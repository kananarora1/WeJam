package com.example.wejam.venue.repository;

import com.example.wejam.venue.dto.VenueSummary;
import com.example.wejam.venue.model.Venue;
import com.example.wejam.venue.dto.VenueVerificationRow;
import com.example.wejam.venue.model.VerificationStatus;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VenueRepository extends JpaRepository<Venue, UUID> {

    Optional<Venue> findByIdAndOwnerId(UUID id, UUID ownerId);

    /** One query with a space count per venue (DTO projection, no entities loaded). */
    @Query("""
            SELECT new com.example.wejam.venue.dto.VenueSummary(v.id, v.name, v.city, v.verificationStatus, COUNT(s))
            FROM Venue v LEFT JOIN Space s ON s.venue = v
            WHERE v.ownerId = :ownerId
            GROUP BY v.id, v.name, v.city, v.verificationStatus, v.createdAt
            ORDER BY v.createdAt
            """)
    List<VenueSummary> findSummariesByOwnerId(UUID ownerId);

    /** Review queue with the duplicate-FSSAI check computed in the same query. */
    @Query("""
            SELECT new com.example.wejam.venue.dto.VenueVerificationRow(
                v.id, v.name, v.city, v.ownerId, v.fssaiNumber, v.verificationStatus, v.verificationRequestedAt,
                (SELECT COUNT(o) FROM Venue o WHERE o.fssaiNumber = v.fssaiNumber AND o.id <> v.id))
            FROM Venue v
            WHERE v.verificationStatus = :status
            ORDER BY v.verificationRequestedAt, v.id
            """)
    List<VenueVerificationRow> findVerificationQueue(VerificationStatus status, Limit limit);

    @Query("SELECT COUNT(o) FROM Venue o WHERE o.fssaiNumber = :fssaiNumber AND o.id <> :venueId")
    long countOtherVenuesWithFssai(String fssaiNumber, UUID venueId);

    /** Conditional: only a PENDING venue can be decided, so two admins can't both decide it. */
    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE venues SET verification_status = 'VERIFIED', rejection_reason = NULL
            WHERE id = :venueId AND verification_status = 'PENDING'
            """, nativeQuery = true)
    int approvePending(UUID venueId);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE venues SET verification_status = 'REJECTED', rejection_reason = :reason
            WHERE id = :venueId AND verification_status = 'PENDING'
            """, nativeQuery = true)
    int rejectPending(UUID venueId, String reason);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE venues SET verification_status = 'PENDING', rejection_reason = NULL, verification_requested_at = now()
            WHERE id = :venueId AND owner_id = :ownerId AND verification_status = 'REJECTED'
            """, nativeQuery = true)
    int resubmitRejected(UUID venueId, UUID ownerId);
}
