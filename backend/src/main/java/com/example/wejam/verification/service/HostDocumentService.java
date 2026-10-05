package com.example.wejam.verification.service;

import com.example.wejam.host.dto.HostVerificationState;
import com.example.wejam.host.exception.HostVerificationStateException;
import com.example.wejam.host.model.HostVerificationStatus;
import com.example.wejam.host.model.IdType;
import com.example.wejam.host.service.HostProfileService;
import com.example.wejam.verification.config.VerificationProperties;
import com.example.wejam.verification.dto.DocumentResponse;
import com.example.wejam.verification.dto.DocumentUploadRequest;
import com.example.wejam.verification.dto.DocumentUploadResponse;
import com.example.wejam.verification.dto.HostVerificationResponse;
import com.example.wejam.verification.exception.DocumentException;
import com.example.wejam.verification.model.DocumentOwnerType;
import com.example.wejam.verification.model.DocumentType;
import com.example.wejam.verification.model.VerificationDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * A host's own, optional ID verification (design 4.3): upload the ID, then ask for review. ID files change only
 * while NOT_REQUESTED or REJECTED, so an admin always reviews exactly what was submitted.
 */
@Service
@Transactional
public class HostDocumentService {

    private static final Set<DocumentType> ID_TYPES = EnumSet.of(DocumentType.ID_FRONT, DocumentType.ID_BACK);

    private final VerificationDocumentStore store;
    private final HostProfileService hostProfiles;
    private final VerificationProperties properties;

    public HostDocumentService(VerificationDocumentStore store, HostProfileService hostProfiles,
                               VerificationProperties properties) {
        this.store = store;
        this.hostProfiles = hostProfiles;
        this.properties = properties;
    }

    public DocumentUploadResponse startUpload(UUID ownerId, DocumentUploadRequest request) {
        UUID profileId = editableProfile(ownerId);
        if (!ID_TYPES.contains(request.type())) {
            throw DocumentException.wrongDocumentType("is not an ID side (ID_FRONT or ID_BACK)");
        }
        return store.startUpload(DocumentOwnerType.HOST_PROFILE, profileId, request);
    }

    public DocumentResponse confirm(UUID ownerId, UUID documentId) {
        UUID profileId = editableProfile(ownerId);
        VerificationDocument document = store.confirm(DocumentOwnerType.HOST_PROFILE, profileId, documentId);
        // Privacy by default: an ID that's never submitted for review doesn't stay with us forever.
        document.scheduleDeletion(Instant.now().plus(properties.idRetention()));
        return store.toResponse(document);
    }

    public void delete(UUID ownerId, UUID documentId) {
        store.delete(DocumentOwnerType.HOST_PROFILE, editableProfile(ownerId), documentId);
    }

    @Transactional(readOnly = true)
    public HostVerificationResponse status(UUID ownerId) {
        HostVerificationState state = hostProfiles.myVerification(ownerId);
        return new HostVerificationResponse(state.status(), state.idType(), state.rejectionReason(),
                state.requestedAt(), store.uploaded(DocumentOwnerType.HOST_PROFILE, state.profileId()));
    }

    public HostVerificationResponse submit(UUID ownerId, IdType idType) {
        HostVerificationState state = hostProfiles.myVerification(ownerId);
        if (!state.status().canRequest()) {
            throw HostVerificationStateException.alreadyRequested();
        }
        // Under review: keep the files until there's a decision. Doing this before the check below also means
        // that if the cleanup job is deleting them right now, we wait for it and then correctly find nothing.
        store.setDeleteAfter(DocumentOwnerType.HOST_PROFILE, state.profileId(), null);
        if (!store.hasUploaded(DocumentOwnerType.HOST_PROFILE, state.profileId(), DocumentType.ID_FRONT)) {
            throw DocumentException.idFrontRequired();
        }
        hostProfiles.requestVerification(ownerId, idType);
        return status(ownerId);
    }

    private UUID editableProfile(UUID ownerId) {
        HostVerificationState state = hostProfiles.myVerification(ownerId);
        if (state.status() == HostVerificationStatus.VERIFIED) {
            throw DocumentException.hostAlreadyVerified();
        }
        if (state.status() == HostVerificationStatus.PENDING) {
            throw DocumentException.underReview();
        }
        return state.profileId();
    }
}
