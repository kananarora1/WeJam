package com.example.wejam.host.model;

import com.example.wejam.common.model.Genre;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "host_profiles")
public class HostProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    private HostType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_kind")
    private GroupKind groupKind;

    @Column(name = "group_name")
    private String groupName;

    private String bio;
    private String area;

    @Column(name = "instagram_handle")
    private String instagramHandle;

    /** Invited + accepted members (owner excluded). Read-only here: only the atomic counter queries write it,
     *  so saving this entity can never overwrite a concurrent change with a stale value. */
    @Column(name = "member_count", insertable = false, updatable = false)
    private int memberCount;

    // Verification fields are read-only here: only the conditional updates in HostProfileRepository write them,
    // so saving a profile edit can never overwrite an admin's decision with a stale value.
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", insertable = false, updatable = false)
    private HostVerificationStatus verificationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "id_type", insertable = false, updatable = false)
    private IdType idType;

    @Column(name = "verification_requested_at", insertable = false, updatable = false)
    private Instant verificationRequestedAt;

    @Column(name = "rejection_reason", insertable = false, updatable = false)
    private String rejectionReason;

    @ElementCollection
    @CollectionTable(name = "host_profile_genres", joinColumns = @JoinColumn(name = "host_profile_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "genre")
    private Set<Genre> genres = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "host_media_links", joinColumns = @JoinColumn(name = "host_profile_id"))
    @OrderColumn(name = "position")
    private List<MediaLink> mediaLinks = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    protected HostProfile() {
    }

    public HostProfile(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public void becomeIndividual() {
        this.type = HostType.INDIVIDUAL;
        this.groupKind = null;
        this.groupName = null;
    }

    public void becomeGroup(GroupKind kind, String name) {
        this.type = HostType.GROUP;
        this.groupKind = kind;
        this.groupName = name;
    }

    public void updateAbout(String bio, String area, String instagramHandle, Set<Genre> genres,
                            List<MediaLink> mediaLinks) {
        this.bio = bio;
        this.area = area;
        this.instagramHandle = instagramHandle;
        // Replace contents, not the collections: Hibernate tracks these instances.
        this.genres.clear();
        this.genres.addAll(genres);
        this.mediaLinks.clear();
        this.mediaLinks.addAll(mediaLinks);
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public HostType getType() {
        return type;
    }

    public GroupKind getGroupKind() {
        return groupKind;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getBio() {
        return bio;
    }

    public String getArea() {
        return area;
    }

    public String getInstagramHandle() {
        return instagramHandle;
    }

    public int getMemberCount() {
        return memberCount;
    }

    /** Null right after the first insert (the DB default applies), which means NOT_REQUESTED. */
    public HostVerificationStatus getVerificationStatus() {
        return verificationStatus == null ? HostVerificationStatus.NOT_REQUESTED : verificationStatus;
    }

    public IdType getIdType() {
        return idType;
    }

    public Instant getVerificationRequestedAt() {
        return verificationRequestedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    /** In the enum's order, so responses are stable. */
    public List<Genre> getGenres() {
        return genres.isEmpty() ? List.of() : List.copyOf(EnumSet.copyOf(genres));
    }

    public List<MediaLink> getMediaLinks() {
        return List.copyOf(mediaLinks);
    }
}
