package com.example.wejam.host.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;

/** Read model only: all writes are atomic native statements in HostGroupMemberRepository. */
@Entity
@Table(name = "host_group_members")
public class HostGroupMember {

    @EmbeddedId
    private HostGroupMemberId id;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Column(name = "invited_at")
    private Instant invitedAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    protected HostGroupMember() {
    }

    public HostGroupMemberId getId() {
        return id;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public Instant getInvitedAt() {
        return invitedAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }
}
