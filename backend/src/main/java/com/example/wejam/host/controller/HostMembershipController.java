package com.example.wejam.host.controller;

import com.example.wejam.common.web.CurrentUser;
import com.example.wejam.host.dto.HostInviteResponse;
import com.example.wejam.host.dto.InviteMemberRequest;
import com.example.wejam.host.dto.PendingInviteResponse;
import com.example.wejam.host.service.HostMembershipService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Hosts")
@RestController
@RequestMapping("/api/v1/me")
class HostMembershipController {

    private final HostMembershipService membershipService;

    HostMembershipController(HostMembershipService membershipService) {
        this.membershipService = membershipService;
    }

    // --- As the group owner

    @PostMapping("/host-profile/invites")
    @ResponseStatus(HttpStatus.CREATED)
    PendingInviteResponse inviteMember(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody InviteMemberRequest request) {
        return membershipService.invite(CurrentUser.id(jwt), request.phoneNumber());
    }

    @GetMapping("/host-profile/invites")
    List<PendingInviteResponse> pendingInvites(@AuthenticationPrincipal Jwt jwt) {
        return membershipService.pendingInvites(CurrentUser.id(jwt));
    }

    /** Removes a member or cancels a pending invite. */
    @DeleteMapping("/host-profile/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void removeMember(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId) {
        membershipService.removeMember(CurrentUser.id(jwt), userId);
    }

    // --- As an invitee / member

    @GetMapping("/host-invites")
    List<HostInviteResponse> myHostInvites(@AuthenticationPrincipal Jwt jwt) {
        return membershipService.myInvites(CurrentUser.id(jwt));
    }

    @PostMapping("/host-invites/{hostProfileId}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void acceptHostInvite(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID hostProfileId) {
        membershipService.accept(CurrentUser.id(jwt), hostProfileId);
    }

    @PostMapping("/host-invites/{hostProfileId}/decline")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void declineHostInvite(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID hostProfileId) {
        membershipService.decline(CurrentUser.id(jwt), hostProfileId);
    }

    @DeleteMapping("/host-memberships/{hostProfileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void leaveHostGroup(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID hostProfileId) {
        membershipService.leave(CurrentUser.id(jwt), hostProfileId);
    }
}
