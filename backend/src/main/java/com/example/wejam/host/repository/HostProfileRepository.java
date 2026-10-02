package com.example.wejam.host.repository;

import com.example.wejam.host.model.HostProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HostProfileRepository extends JpaRepository<HostProfile, UUID> {

    /** Genres and links in the same query (a set plus an indexed list, so no bag-fetch problem). */
    @EntityGraph(attributePaths = {"genres", "mediaLinks"})
    Optional<HostProfile> findWithDetailsById(UUID id);

    @EntityGraph(attributePaths = {"genres", "mediaLinks"})
    Optional<HostProfile> findWithDetailsByOwnerId(UUID ownerId);
}
