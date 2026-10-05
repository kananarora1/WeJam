package com.example.wejam.verification.repository;

import com.example.wejam.verification.model.DocumentOwnerType;
import com.example.wejam.verification.model.DocumentStatus;
import com.example.wejam.verification.model.DocumentType;
import com.example.wejam.verification.model.VerificationDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerificationDocumentRepository extends JpaRepository<VerificationDocument, UUID> {

    Optional<VerificationDocument> findByIdAndOwnerTypeAndOwnerId(UUID id, DocumentOwnerType ownerType, UUID ownerId);

    Optional<VerificationDocument> findByOwnerTypeAndOwnerIdAndDocTypeAndStatus(
            DocumentOwnerType ownerType, UUID ownerId, DocumentType docType, DocumentStatus status);

    boolean existsByOwnerTypeAndOwnerIdAndDocTypeAndStatus(
            DocumentOwnerType ownerType, UUID ownerId, DocumentType docType, DocumentStatus status);

    List<VerificationDocument> findByOwnerTypeAndOwnerIdAndStatusOrderByDocType(
            DocumentOwnerType ownerType, UUID ownerId, DocumentStatus status);

    List<VerificationDocument> findByOwnerTypeAndOwnerId(DocumentOwnerType ownerType, UUID ownerId);

    /** Sets (or clears, with null) the deletion time of an owner's uploaded documents. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE VerificationDocument d SET d.deleteAfter = :at
            WHERE d.ownerType = :ownerType AND d.ownerId = :ownerId
              AND d.status = com.example.wejam.verification.model.DocumentStatus.UPLOADED
            """)
    int setDeleteAfter(DocumentOwnerType ownerType, UUID ownerId, Instant at);

    /**
     * Documents due for deletion: past their retention, or uploads started but never confirmed.
     * FOR UPDATE SKIP LOCKED: rows stay locked until the caller's transaction ends (so a host can't put them
     * back under review mid-delete), and a second app instance running the same job skips them instead of
     * waiting — the classic Postgres job-queue pattern.
     */
    @Query(value = """
            SELECT * FROM verification_documents
            WHERE delete_after < :now
               OR (status = 'PENDING_UPLOAD' AND created_at < :abandonedBefore)
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<VerificationDocument> lockDueForDeletion(Instant now, Instant abandonedBefore, int limit);
}
