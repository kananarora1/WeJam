package com.example.wejam.venue.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "spaces")
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false, updatable = false)
    private Venue venue;

    private String name;
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "sound_policy")
    private SoundPolicy soundPolicy;

    /** Local time of day in the venue's time zone; null = no curfew. */
    @Column(name = "sound_curfew")
    private LocalTime soundCurfew;

    @Column(name = "house_rules")
    private String houseRules;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Space() {
    }

    public Space(Venue venue) {
        this.venue = venue;
    }

    public void update(String name, int capacity, SoundPolicy soundPolicy, LocalTime soundCurfew, String houseRules) {
        this.name = name;
        this.capacity = capacity;
        this.soundPolicy = soundPolicy;
        this.soundCurfew = soundCurfew;
        this.houseRules = houseRules;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public SoundPolicy getSoundPolicy() {
        return soundPolicy;
    }

    public LocalTime getSoundCurfew() {
        return soundCurfew;
    }

    public String getHouseRules() {
        return houseRules;
    }
}
