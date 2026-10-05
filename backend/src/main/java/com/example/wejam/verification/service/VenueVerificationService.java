package com.example.wejam.verification.service;

import com.example.wejam.admin.model.AdminActionType;
import com.example.wejam.admin.model.AdminTargetType;
import com.example.wejam.admin.service.AdminActionService;
import com.example.wejam.auth.dto.UserSummary;
import com.example.wejam.auth.service.UserService;
import com.example.wejam.venue.dto.VenueResponse;
import com.example.wejam.venue.dto.VenueVerificationRow;
import com.example.wejam.venue.model.VerificationIssue;
import com.example.wejam.venue.model.VerificationStatus;
import com.example.wejam.venue.service.VenueService;
import com.example.wejam.verification.dto.VenueReviewResponse;
import com.example.wejam.verification.dto.VenueVerificationItem;
import com.example.wejam.verification.dto.VerificationChecks;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Platform admin: review venues. Status writes stay in the venue module; this records who decided what. */
@Service
@Transactional
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class VenueVerificationService {

    static final int QUEUE_LIMIT = 100;

    private final VenueService venueService;
    private final UserService userService;
    private final AdminActionService adminActions;
    private final VenueDocumentService documents;

    public VenueVerificationService(VenueService venueService, UserService userService,
                                    AdminActionService adminActions, VenueDocumentService documents) {
        this.venueService = venueService;
        this.userService = userService;
        this.adminActions = adminActions;
        this.documents = documents;
    }

    /** Oldest request first. */
    @Transactional(readOnly = true)
    public List<VenueVerificationItem> queue(VerificationStatus status) {
        List<VenueVerificationRow> rows = venueService.verificationQueue(status, QUEUE_LIMIT);
        Map<UUID, UserSummary> owners = userService.summaries(rows.stream().map(VenueVerificationRow::ownerId).distinct().toList());
        return rows.stream().map(r -> new VenueVerificationItem(r.venueId(), r.name(), r.city(),
                nameOf(owners.get(r.ownerId())), r.fssaiNumber(), r.status(), r.requestedAt(),
                new VerificationChecks(FssaiChecks.structureLooksValid(r.fssaiNumber()),
                        r.otherVenuesWithSameFssai()))).toList();
    }

    @Transactional(readOnly = true)
    public VenueReviewResponse review(UUID venueId) {
        VenueResponse venue = venueService.get(venueId);
        UUID ownerId = venueService.ownerOf(venueId);
        UserSummary owner = userService.summaries(List.of(ownerId)).get(ownerId);
        VerificationChecks checks = new VerificationChecks(FssaiChecks.structureLooksValid(venue.fssaiNumber()),
                venueService.countOtherVenuesWithFssai(venueId, venue.fssaiNumber()));
        return new VenueReviewResponse(venue, nameOf(owner), owner == null ? null : owner.phone(), checks,
                documents.uploadedDocuments(venueId));
    }

    public VenueResponse approve(UUID adminId, UUID venueId) {
        venueService.approveVerification(venueId);
        adminActions.record(adminId, AdminActionType.VENUE_APPROVED, AdminTargetType.VENUE, venueId, null);
        return venueService.get(venueId);
    }

    public VenueResponse reject(UUID adminId, UUID venueId, String reason, Set<VerificationIssue> issues) {
        String trimmed = reason.strip();
        venueService.rejectVerification(venueId, trimmed, issues);
        adminActions.record(adminId, AdminActionType.VENUE_REJECTED, AdminTargetType.VENUE, venueId, trimmed);
        return venueService.get(venueId);
    }

    private static String nameOf(UserSummary user) {
        return user == null ? null : user.displayName();
    }
}
