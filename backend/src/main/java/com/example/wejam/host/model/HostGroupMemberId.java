package com.example.wejam.host.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record HostGroupMemberId(
        @Column(name = "host_profile_id") UUID hostProfileId,
        @Column(name = "user_id") UUID userId) implements Serializable {
}
