package com.example.wejam.host.repository;

import com.example.wejam.host.dto.HostVerificationRow;
import com.example.wejam.host.model.HostProfile;
import com.example.wejam.host.model.HostVerificationStatus;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HostProfileRepository extends JpaRepository<HostProfile, UUID> {

    /** Genres and links in the same query (a set plus an indexed list, so no bag-fetch problem). */
    @EntityGraph(attributePaths = {"genres", "mediaLinks"})
    Optional<HostProfile> findWithDetailsById(UUID id);

    @EntityGraph(attributePaths = {"genres", "mediaLinks"})
    Optional<HostProfile> findWithDetailsByOwnerId(UUID ownerId);

    Optional<HostProfile> findByOwnerId(UUID ownerId);

    /** Atomic cap (CLAUDE.md §5.4): 0 = the group is full. Never count-then-insert. */
    @Modifying
    @Query(value = "UPDATE host_profiles SET member_count = member_count + 1 WHERE id = :id AND member_count < :max",
            nativeQuery = true)
    int reserveMemberSlot(UUID id, int max);

    @Modifying
    @Query(value = "UPDATE host_profiles SET member_count = member_count - 1 WHERE id = :id", nativeQuery = true)
    int releaseMemberSlot(UUID id);

    @Query("""
            SELECT new com.example.wejam.host.dto.HostVerificationRow(
                h.id, h.type, h.groupName, h.ownerId, h.idType, h.verificationStatus, h.verificationRequestedAt)
            FROM HostProfile h
            WHERE h.verificationStatus = :status
            ORDER BY h.verificationRequestedAt, h.id
            """)
    List<HostVerificationRow> findVerificationQueue(HostVerificationStatus status, Limit limit);

    // Verification transitions are single conditional UPDATEs (ADR-027): 0 rows = not in the expected state,
    // so two requests or two admins can never both win.

    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE host_profiles SET verification_status = 'PENDING', id_type = :idType, rejection_reason = NULL,
                                     verification_requested_at = now()
            WHERE owner_id = :ownerId AND verification_status IN ('NOT_REQUESTED', 'REJECTED')
            """, nativeQuery = true)
    int requestVerification(UUID ownerId, String idType);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE host_profiles SET verification_status = 'VERIFIED', rejection_reason = NULL
            WHERE id = :profileId AND verification_status = 'PENDING'
            """, nativeQuery = true)
    int approvePending(UUID profileId);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE host_profiles SET verification_status = 'REJECTED', rejection_reason = :reason
            WHERE id = :profileId AND verification_status = 'PENDING'
            """, nativeQuery = true)
    int rejectPending(UUID profileId, String reason);
}
