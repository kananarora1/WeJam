package com.example.wejam.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

/** Append-only audit record of a platform-admin decision. */
@Entity
@Immutable
@Table(name = "admin_actions")
public class AdminAction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "admin_id", nullable = false)
    private UUID adminId;

    @Enumerated(EnumType.STRING)
    private AdminActionType action;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    private AdminTargetType targetType;

    @Column(name = "target_id")
    private UUID targetId;

    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected AdminAction() {
    }

    public AdminAction(UUID adminId, AdminActionType action, AdminTargetType targetType, UUID targetId,
                       String reason) {
        this.adminId = adminId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAdminId() {
        return adminId;
    }

    public AdminActionType getAction() {
        return action;
    }

    public AdminTargetType getTargetType() {
        return targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
