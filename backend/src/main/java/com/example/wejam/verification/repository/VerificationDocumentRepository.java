package com.example.wejam.verification.repository;

import com.example.wejam.verification.model.DocumentOwnerType;
import com.example.wejam.verification.model.DocumentStatus;
import com.example.wejam.verification.model.DocumentType;
import com.example.wejam.verification.model.VerificationDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerificationDocumentRepository extends JpaRepository<VerificationDocument, UUID> {

    Optional<VerificationDocument> findByIdAndOwnerTypeAndOwnerId(UUID id, DocumentOwnerType ownerType, UUID ownerId);

    Optional<VerificationDocument> findByOwnerTypeAndOwnerIdAndDocTypeAndStatus(
            DocumentOwnerType ownerType, UUID ownerId, DocumentType docType, DocumentStatus status);

    List<VerificationDocument> findByOwnerTypeAndOwnerIdAndStatusOrderByDocType(
            DocumentOwnerType ownerType, UUID ownerId, DocumentStatus status);

    List<VerificationDocument> findByOwnerTypeAndOwnerId(DocumentOwnerType ownerType, UUID ownerId);
}
