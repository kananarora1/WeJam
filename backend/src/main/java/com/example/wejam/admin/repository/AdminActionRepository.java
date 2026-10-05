package com.example.wejam.admin.repository;

import com.example.wejam.admin.model.AdminAction;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AdminActionRepository extends JpaRepository<AdminAction, UUID> {

    @Query("SELECT a FROM AdminAction a ORDER BY a.createdAt DESC, a.id DESC")
    List<AdminAction> findNewest(Limit limit);

    /** Keyset pagination: everything strictly older than the last row of the previous page (no OFFSET). */
    @Query("""
            SELECT a FROM AdminAction a
            WHERE a.createdAt < :createdAt OR (a.createdAt = :createdAt AND a.id < :id)
            ORDER BY a.createdAt DESC, a.id DESC
            """)
    List<AdminAction> findOlderThan(Instant createdAt, UUID id, Limit limit);
}
