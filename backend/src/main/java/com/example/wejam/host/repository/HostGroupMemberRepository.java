package com.example.wejam.host.repository;

import com.example.wejam.host.model.HostGroupMember;
import com.example.wejam.host.model.HostGroupMemberId;
import com.example.wejam.host.model.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface HostGroupMemberRepository extends JpaRepository<HostGroupMember, HostGroupMemberId> {

    List<HostGroupMember> findByIdHostProfileIdAndStatusOrderByInvitedAtAsc(UUID hostProfileId, MemberStatus status);

    List<HostGroupMember> findByIdHostProfileIdAndStatusOrderByAcceptedAtAsc(UUID hostProfileId, MemberStatus status);

    List<HostGroupMember> findByIdUserIdAndStatusOrderByInvitedAtAsc(UUID userId, MemberStatus status);

    /** 0 = already invited or already a member. */
    @Modifying
    @Query(value = """
            INSERT INTO host_group_members (host_profile_id, user_id, status)
            VALUES (:hostProfileId, :userId, 'INVITED')
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int insertInvite(UUID hostProfileId, UUID userId);

    /** Conditional, so accepting twice (or a non-pending row) changes nothing and returns 0. */
    @Modifying
    @Query(value = """
            UPDATE host_group_members SET status = 'ACCEPTED', accepted_at = now()
            WHERE host_profile_id = :hostProfileId AND user_id = :userId AND status = 'INVITED'
            """, nativeQuery = true)
    int accept(UUID hostProfileId, UUID userId);

    @Modifying
    @Query(value = """
            DELETE FROM host_group_members
            WHERE host_profile_id = :hostProfileId AND user_id = :userId AND status = :status
            """, nativeQuery = true)
    int deleteWithStatus(UUID hostProfileId, UUID userId, String status);

    @Modifying
    @Query(value = "DELETE FROM host_group_members WHERE host_profile_id = :hostProfileId AND user_id = :userId",
            nativeQuery = true)
    int deleteMember(UUID hostProfileId, UUID userId);
}
