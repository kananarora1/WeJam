package com.example.wejam.auth.repository;

import com.example.wejam.auth.dto.UserSummary;
import com.example.wejam.auth.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @EntityGraph(attributePaths = "roles")
    Optional<User> findWithRolesByFirebaseUid(String firebaseUid);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findWithRolesById(UUID id);

    /** Returns 1 if the user was created, 0 if it already existed. Safe under concurrent first logins. */
    @Modifying
    @Query(value = """
            INSERT INTO users (firebase_uid, phone)
            VALUES (:firebaseUid, :phone)
            ON CONFLICT (firebase_uid) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(String firebaseUid, String phone);

    @Modifying
    @Query(value = """
            INSERT INTO user_roles (user_id, role)
            SELECT id, :role FROM users WHERE firebase_uid = :firebaseUid
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int addRoleByFirebaseUid(String firebaseUid, String role);

    @Modifying
    @Query(value = """
            UPDATE users SET display_name = :displayName, updated_at = now()
            WHERE id = :userId
            """, nativeQuery = true)
    int updateDisplayName(UUID userId, String displayName);

    /** Idempotent: re-adding an existing role is a no-op. */
    @Modifying
    @Query(value = """
            INSERT INTO user_roles (user_id, role)
            VALUES (:userId, :role)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int addRole(UUID userId, String role);

    @Query("SELECT new com.example.wejam.auth.dto.UserSummary(u.id, u.displayName, u.phone) FROM User u WHERE u.phone = :phone")
    Optional<UserSummary> findSummaryByPhone(String phone);

    @Query("SELECT new com.example.wejam.auth.dto.UserSummary(u.id, u.displayName, u.phone) FROM User u WHERE u.id IN :ids")
    List<UserSummary> findSummariesByIdIn(Collection<UUID> ids);
}
