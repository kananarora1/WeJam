package com.example.wejam.host.repository;

import com.example.wejam.host.model.HostProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface HostProfileRepository extends JpaRepository<HostProfile, UUID> {

    /** Genres and links in the same query (a set plus an indexed list, so no bag-fetch problem). */
    @EntityGraph(attributePaths = {"genres", "mediaLinks"})
    Optional<HostProfile> findWithDetailsById(UUID id);

    @EntityGraph(attributePaths = {"genres", "mediaLinks"})
    Optional<HostProfile> findWithDetailsByOwnerId(UUID ownerId);

    Optional<HostProfile> findByOwnerId(UUID ownerId);

    /** Atomic cap (CLAUDE.md §5.4): 0 = the group is full. Never count-then-insert. */
    @Modifying
    @Query(value = "UPDATE host_profiles SET member_count = member_count + 1 WHERE id = :id AND member_count < :max",
            nativeQuery = true)
    int reserveMemberSlot(UUID id, int max);

    @Modifying
    @Query(value = "UPDATE host_profiles SET member_count = member_count - 1 WHERE id = :id", nativeQuery = true)
    int releaseMemberSlot(UUID id);
}
