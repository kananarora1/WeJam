package com.example.wejam.venue.repository;

import com.example.wejam.venue.model.Space;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpaceRepository extends JpaRepository<Space, UUID> {

    List<Space> findByVenueIdOrderByCreatedAtAscIdAsc(UUID venueId);

    Optional<Space> findByIdAndVenueId(UUID id, UUID venueId);
}
