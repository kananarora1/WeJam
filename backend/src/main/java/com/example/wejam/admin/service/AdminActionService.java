package com.example.wejam.admin.service;

import com.example.wejam.admin.dto.AdminActionPage;
import com.example.wejam.admin.dto.AdminActionResponse;
import com.example.wejam.admin.model.AdminAction;
import com.example.wejam.admin.model.AdminActionType;
import com.example.wejam.admin.model.AdminTargetType;
import com.example.wejam.admin.repository.AdminActionRepository;
import com.example.wejam.auth.dto.UserSummary;
import com.example.wejam.auth.service.UserService;
import org.springframework.data.domain.Limit;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminActionService {

    static final int PAGE_SIZE = 50;

    private final AdminActionRepository repository;
    private final UserService userService;

    public AdminActionService(AdminActionRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    /**
     * MANDATORY: must join the caller's transaction, so the log row and the decision commit or roll back
     * together — there is never a decision without its log entry, or a log entry for a decision that failed.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(UUID adminId, AdminActionType action, AdminTargetType targetType, UUID targetId,
                       String reason) {
        repository.save(new AdminAction(adminId, action, targetType, targetId, reason));
    }

    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Transactional(readOnly = true)
    public AdminActionPage page(Instant before, UUID beforeId) {
        List<AdminAction> rows = before == null || beforeId == null
                ? repository.findNewest(Limit.of(PAGE_SIZE))
                : repository.findOlderThan(before, beforeId, Limit.of(PAGE_SIZE));
        Map<UUID, UserSummary> admins = userService.summaries(rows.stream().map(AdminAction::getAdminId).distinct().toList());
        List<AdminActionResponse> items = rows.stream().map(a -> new AdminActionResponse(a.getId(), a.getAdminId(),
                admins.containsKey(a.getAdminId()) ? admins.get(a.getAdminId()).displayName() : null,
                a.getAction(), a.getTargetType(), a.getTargetId(), a.getReason(), a.getCreatedAt())).toList();
        AdminAction last = rows.size() == PAGE_SIZE ? rows.getLast() : null;
        return new AdminActionPage(items, last == null ? null : last.getCreatedAt(), last == null ? null : last.getId());
    }
}
