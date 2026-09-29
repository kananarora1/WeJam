package com.example.wejam.auth;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Rows are inserted via native upserts in {@link UserRepository}, so this entity is read-only for now
 * and ids/timestamps come from DB defaults.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    @Column(name = "firebase_uid", nullable = false, updatable = false)
    private String firebaseUid;

    private String phone;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    @ElementCollection
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    protected User() {
    }

    public UUID getId() {
        return id;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public String getPhone() {
        return phone;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Set<Role> getRoles() {
        return Set.copyOf(roles);
    }
}
