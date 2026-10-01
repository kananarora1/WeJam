package com.example.wejam.venue;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpaceRepository extends JpaRepository<Space, UUID> {

    /** Spaces with their gear in one query (no N+1 across spaces). */
    @EntityGraph(attributePaths = "gear")
    List<Space> findByVenueIdOrderByCreatedAtAscIdAsc(UUID venueId);

    Optional<Space> findByIdAndVenueId(UUID id, UUID venueId);
}
