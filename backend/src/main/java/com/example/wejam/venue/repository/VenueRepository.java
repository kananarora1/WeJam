package com.example.wejam.venue.repository;

import com.example.wejam.venue.dto.VenueSummary;
import com.example.wejam.venue.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VenueRepository extends JpaRepository<Venue, UUID> {

    Optional<Venue> findByIdAndOwnerId(UUID id, UUID ownerId);

    /** One query with a space count per venue (DTO projection, no entities loaded). */
    @Query("""
            SELECT new com.example.wejam.venue.dto.VenueSummary(v.id, v.name, v.city, COUNT(s))
            FROM Venue v LEFT JOIN Space s ON s.venue = v
            WHERE v.ownerId = :ownerId
            GROUP BY v.id, v.name, v.city, v.createdAt
            ORDER BY v.createdAt
            """)
    List<VenueSummary> findSummariesByOwnerId(UUID ownerId);
}
