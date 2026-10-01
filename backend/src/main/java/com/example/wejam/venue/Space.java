package com.example.wejam.venue;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

    @ElementCollection
    @CollectionTable(name = "space_gear", joinColumns = @JoinColumn(name = "space_id"))
    @OrderColumn(name = "position")
    private List<GearItem> gear = new ArrayList<>();

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

    public void update(String name, int capacity, List<GearItem> gear) {
        this.name = name;
        this.capacity = capacity;
        // Replace contents rather than the list itself: Hibernate tracks this collection instance.
        this.gear.clear();
        this.gear.addAll(gear);
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

    public List<GearItem> getGear() {
        return List.copyOf(gear);
    }
}
