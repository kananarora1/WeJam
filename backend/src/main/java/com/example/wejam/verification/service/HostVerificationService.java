package com.example.wejam.verification.service;

import com.example.wejam.admin.model.AdminActionType;
import com.example.wejam.admin.model.AdminTargetType;
import com.example.wejam.admin.service.AdminActionService;
import com.example.wejam.auth.dto.UserSummary;
import com.example.wejam.auth.service.UserService;
import com.example.wejam.host.dto.HostVerificationRow;
import com.example.wejam.host.dto.HostVerificationState;
import com.example.wejam.host.model.HostType;
import com.example.wejam.host.model.HostVerificationStatus;
import com.example.wejam.host.service.HostProfileService;
import com.example.wejam.verification.config.VerificationProperties;
import com.example.wejam.verification.dto.HostReviewResponse;
import com.example.wejam.verification.dto.HostVerificationItem;
import com.example.wejam.verification.model.DocumentOwnerType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Platform admin: review host IDs. Status writes stay in the host module; this records who decided what. */
@Service
@Transactional
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class HostVerificationService {

    static final int QUEUE_LIMIT = 100;

    private final HostProfileService hostProfiles;
    private final UserService userService;
    private final AdminActionService adminActions;
    private final VerificationDocumentStore store;
    private final VerificationProperties properties;

    public HostVerificationService(HostProfileService hostProfiles, UserService userService,
                                   AdminActionService adminActions, VerificationDocumentStore store,
                                   VerificationProperties properties) {
        this.hostProfiles = hostProfiles;
        this.userService = userService;
        this.adminActions = adminActions;
        this.store = store;
        this.properties = properties;
    }

    /** Oldest request first. */
    @Transactional(readOnly = true)
    public List<HostVerificationItem> queue(HostVerificationStatus status) {
        List<HostVerificationRow> rows = hostProfiles.verificationQueue(status, QUEUE_LIMIT);
        Map<UUID, UserSummary> owners = userService.summaries(rows.stream().map(HostVerificationRow::ownerId).distinct().toList());
        return rows.stream().map(r -> {
            String ownerName = nameOf(owners.get(r.ownerId()));
            return new HostVerificationItem(r.profileId(), r.type() == HostType.GROUP ? r.groupName() : ownerName,
                    r.type(), ownerName, r.idType(), r.status(), r.requestedAt());
        }).toList();
    }

    @Transactional(readOnly = true)
    public HostReviewResponse review(UUID profileId) {
        HostVerificationState state = hostProfiles.verificationState(profileId);
        UserSummary owner = userService.summaries(List.of(state.ownerId())).get(state.ownerId());
        return new HostReviewResponse(hostProfiles.get(profileId), nameOf(owner), owner == null ? null : owner.phone(),
                state.status(), state.idType(), state.rejectionReason(), state.requestedAt(),
                store.uploaded(DocumentOwnerType.HOST_PROFILE, profileId));
    }

    public HostReviewResponse approve(UUID adminId, UUID profileId) {
        hostProfiles.approveVerification(profileId);
        startRetentionClock(profileId);
        adminActions.record(adminId, AdminActionType.HOST_APPROVED, AdminTargetType.HOST_PROFILE, profileId, null);
        return review(profileId);
    }

    public HostReviewResponse reject(UUID adminId, UUID profileId, String reason) {
        String trimmed = reason.strip();
        hostProfiles.rejectVerification(profileId, trimmed);
        startRetentionClock(profileId);
        adminActions.record(adminId, AdminActionType.HOST_REJECTED, AdminTargetType.HOST_PROFILE, profileId, trimmed);
        return review(profileId);
    }

    /** "Deleted 30 days after review" (design 4.3) — whatever the decision. The badge outlives the file. */
    private void startRetentionClock(UUID profileId) {
        store.setDeleteAfter(DocumentOwnerType.HOST_PROFILE, profileId, Instant.now().plus(properties.idRetention()));
    }

    private static String nameOf(UserSummary user) {
        return user == null ? null : user.displayName();
    }
}
