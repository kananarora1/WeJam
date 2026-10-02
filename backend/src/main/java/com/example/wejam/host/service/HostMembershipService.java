package com.example.wejam.host.service;

import com.example.wejam.auth.dto.UserSummary;
import com.example.wejam.auth.service.UserService;
import com.example.wejam.host.dto.HostInviteResponse;
import com.example.wejam.host.dto.PendingInviteResponse;
import com.example.wejam.host.exception.HostMembershipException;
import com.example.wejam.host.exception.HostProfileNotFoundException;
import com.example.wejam.host.model.HostGroupMember;
import com.example.wejam.host.model.HostGroupMemberId;
import com.example.wejam.host.model.HostProfile;
import com.example.wejam.host.model.HostType;
import com.example.wejam.host.model.MemberStatus;
import com.example.wejam.host.repository.HostGroupMemberRepository;
import com.example.wejam.host.repository.HostProfileRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Group membership. Every write is a single conditional statement (insert-if-absent, update-if-pending,
 * counter-if-below-cap), so double taps and concurrent requests can't corrupt the state.
 */
@Service
@Transactional
public class HostMembershipService {

    /** 10 people per group = owner + 9 invited/accepted members. */
    static final int MAX_PEOPLE = 10;
    private static final int MAX_OTHER_MEMBERS = MAX_PEOPLE - 1;

    private final HostProfileRepository hostProfileRepository;
    private final HostGroupMemberRepository memberRepository;
    private final UserService userService;

    public HostMembershipService(HostProfileRepository hostProfileRepository,
                                 HostGroupMemberRepository memberRepository, UserService userService) {
        this.hostProfileRepository = hostProfileRepository;
        this.memberRepository = memberRepository;
        this.userService = userService;
    }

    @PreAuthorize("hasRole('HOST')")
    public PendingInviteResponse invite(UUID ownerId, String phoneNumber) {
        HostProfile group = ownProfile(ownerId);
        if (group.getType() != HostType.GROUP) {
            throw HostMembershipException.notAGroup();
        }
        UserSummary invitee = userService.findByPhone(phoneNumber)
                .orElseThrow(HostMembershipException::noUserWithPhone);
        if (invitee.id().equals(ownerId)) {
            throw HostMembershipException.cannotInviteYourself();
        }
        if (hostProfileRepository.reserveMemberSlot(group.getId(), MAX_OTHER_MEMBERS) == 0) {
            throw HostMembershipException.groupFull(MAX_PEOPLE);
        }
        if (memberRepository.insertInvite(group.getId(), invitee.id()) == 0) {
            // The exception rolls back the transaction, which also gives the reserved slot back.
            throw HostMembershipException.alreadyInvited();
        }
        HostGroupMember invite = memberRepository.findById(new HostGroupMemberId(group.getId(), invitee.id()))
                .orElseThrow();
        return new PendingInviteResponse(invitee.id(), maskPhone(invitee.phone()), invite.getInvitedAt());
    }

    @PreAuthorize("hasRole('HOST')")
    @Transactional(readOnly = true)
    public List<PendingInviteResponse> pendingInvites(UUID ownerId) {
        HostProfile profile = ownProfile(ownerId);
        List<HostGroupMember> invites =
                memberRepository.findByIdHostProfileIdAndStatusOrderByInvitedAtAsc(profile.getId(), MemberStatus.INVITED);
        Map<UUID, UserSummary> users = userService.summaries(invites.stream().map(i -> i.getId().userId()).toList());
        return invites.stream()
                .map(i -> new PendingInviteResponse(i.getId().userId(),
                        maskPhone(users.get(i.getId().userId()).phone()), i.getInvitedAt()))
                .toList();
    }

    /** Removes an accepted member or cancels a pending invite. */
    @PreAuthorize("hasRole('HOST')")
    public void removeMember(UUID ownerId, UUID memberUserId) {
        HostProfile profile = ownProfile(ownerId);
        if (memberRepository.deleteMember(profile.getId(), memberUserId) == 0) {
            throw HostMembershipException.memberNotFound();
        }
        hostProfileRepository.releaseMemberSlot(profile.getId());
    }

    @Transactional(readOnly = true)
    public List<HostInviteResponse> myInvites(UUID userId) {
        List<HostGroupMember> invites = memberRepository.findByIdUserIdAndStatusOrderByInvitedAtAsc(userId,
                MemberStatus.INVITED);
        Map<UUID, HostProfile> groups = hostProfileRepository
                .findAllById(invites.stream().map(i -> i.getId().hostProfileId()).toList()).stream()
                .collect(Collectors.toMap(HostProfile::getId, Function.identity()));
        Map<UUID, UserSummary> owners = userService.summaries(
                groups.values().stream().map(HostProfile::getOwnerId).toList());
        return invites.stream().map(i -> {
            HostProfile group = groups.get(i.getId().hostProfileId());
            UserSummary owner = owners.get(group.getOwnerId());
            return new HostInviteResponse(group.getId(), group.getGroupName(), group.getGroupKind(),
                    owner == null ? null : owner.displayName(), i.getInvitedAt());
        }).toList();
    }

    public void accept(UUID userId, UUID hostProfileId) {
        if (memberRepository.accept(hostProfileId, userId) == 0) {
            throw HostMembershipException.inviteNotFound();
        }
    }

    public void decline(UUID userId, UUID hostProfileId) {
        if (memberRepository.deleteWithStatus(hostProfileId, userId, MemberStatus.INVITED.name()) == 0) {
            throw HostMembershipException.inviteNotFound();
        }
        hostProfileRepository.releaseMemberSlot(hostProfileId);
    }

    public void leave(UUID userId, UUID hostProfileId) {
        if (memberRepository.deleteWithStatus(hostProfileId, userId, MemberStatus.ACCEPTED.name()) == 0) {
            throw HostMembershipException.memberNotFound();
        }
        hostProfileRepository.releaseMemberSlot(hostProfileId);
    }

    private HostProfile ownProfile(UUID ownerId) {
        return hostProfileRepository.findByOwnerId(ownerId).orElseThrow(HostProfileNotFoundException::new);
    }

    /** "+911234567890" → "+91 12•••••890": enough for the owner to recognise, not enough to harvest. */
    static String maskPhone(String e164) {
        if (e164 == null || e164.length() < 8) {
            return "•••";
        }
        // Treats the first three characters as the country code; exact for +91, close enough elsewhere.
        String national = e164.substring(3);
        return e164.substring(0, 3) + " " + national.substring(0, 2) + "•".repeat(national.length() - 5)
                + national.substring(national.length() - 3);
    }
}
