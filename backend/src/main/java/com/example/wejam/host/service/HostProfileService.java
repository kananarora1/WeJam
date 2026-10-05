package com.example.wejam.host.service;

import com.example.wejam.auth.dto.UserSummary;
import com.example.wejam.auth.service.UserService;
import com.example.wejam.host.dto.HostMemberDto;
import com.example.wejam.host.dto.HostProfileRequest;
import com.example.wejam.host.dto.HostProfileResponse;
import com.example.wejam.host.dto.HostVerificationRow;
import com.example.wejam.host.dto.HostVerificationState;
import com.example.wejam.host.exception.HostMembershipException;
import com.example.wejam.host.exception.HostProfileNotFoundException;
import com.example.wejam.host.exception.HostVerificationStateException;
import com.example.wejam.host.exception.InvalidHostProfileException;
import com.example.wejam.host.model.HostGroupMember;
import com.example.wejam.host.model.HostProfile;
import com.example.wejam.host.model.HostType;
import com.example.wejam.host.model.HostVerificationStatus;
import com.example.wejam.host.model.IdType;
import com.example.wejam.host.model.MediaLink;
import com.example.wejam.host.model.MemberStatus;
import com.example.wejam.host.repository.HostGroupMemberRepository;
import com.example.wejam.host.repository.HostProfileRepository;
import org.springframework.data.domain.Limit;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class HostProfileService {

    private final HostProfileRepository hostProfileRepository;
    private final HostGroupMemberRepository memberRepository;
    private final UserService userService;

    public HostProfileService(HostProfileRepository hostProfileRepository, HostGroupMemberRepository memberRepository,
                              UserService userService) {
        this.hostProfileRepository = hostProfileRepository;
        this.memberRepository = memberRepository;
        this.userService = userService;
    }

    /** Create or replace the caller's profile (one per user). */
    @PreAuthorize("hasRole('HOST')")
    public HostProfileResponse saveMine(UUID ownerId, HostProfileRequest request) {
        validateGroupFields(request);
        HostProfile profile = hostProfileRepository.findWithDetailsByOwnerId(ownerId)
                .orElseGet(() -> new HostProfile(ownerId));

        if (request.type() == HostType.GROUP) {
            profile.becomeGroup(request.groupKind(), request.groupName().strip());
        } else {
            if (profile.getMemberCount() > 0) {
                throw HostMembershipException.membersExist();
            }
            profile.becomeIndividual();
        }
        List<MediaLink> links = request.mediaLinks().stream()
                .map(l -> new MediaLink(l.url().strip(), blankToNull(l.title())))
                .toList();
        profile.updateAbout(blankToNull(request.bio()), blankToNull(request.area()),
                instagramHandle(request.instagramHandle()), request.genres(), links);

        return toResponse(hostProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public HostProfileResponse getMine(UUID ownerId) {
        return toResponse(hostProfileRepository.findWithDetailsByOwnerId(ownerId)
                .orElseThrow(HostProfileNotFoundException::new));
    }

    /** Public view: what venues see on a request. */
    @Transactional(readOnly = true)
    public HostProfileResponse get(UUID profileId) {
        return toResponse(hostProfileRepository.findWithDetailsById(profileId)
                .orElseThrow(HostProfileNotFoundException::new));
    }

    // --- Verification. Optional for hosts: nothing anywhere checks it before letting a host act.

    /** For modules that attach data to the caller's host profile (e.g. ID documents). 404 if they have none. */
    @Transactional(readOnly = true)
    public HostVerificationState myVerification(UUID ownerId) {
        return verificationState(hostProfileRepository.findByOwnerId(ownerId)
                .orElseThrow(HostProfileNotFoundException::new));
    }

    /** Owner asks for the badge. Only from NOT_REQUESTED or REJECTED; the conditional update is the real guard. */
    public void requestVerification(UUID ownerId, IdType idType) {
        if (hostProfileRepository.requestVerification(ownerId, idType.name()) == 0) {
            throw HostVerificationStateException.alreadyRequested();
        }
    }

    // Decisions, driven by the verification module. Role-checked here too, so no future caller can skip it.

    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Transactional(readOnly = true)
    public List<HostVerificationRow> verificationQueue(HostVerificationStatus status, int limit) {
        return hostProfileRepository.findVerificationQueue(status, Limit.of(limit));
    }

    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Transactional(readOnly = true)
    public HostVerificationState verificationState(UUID profileId) {
        return verificationState(hostProfileRepository.findById(profileId)
                .orElseThrow(HostProfileNotFoundException::new));
    }

    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public void approveVerification(UUID profileId) {
        requireExists(profileId);
        if (hostProfileRepository.approvePending(profileId) == 0) {
            throw HostVerificationStateException.notPending();
        }
    }

    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public void rejectVerification(UUID profileId, String reason) {
        requireExists(profileId);
        if (hostProfileRepository.rejectPending(profileId, reason) == 0) {
            throw HostVerificationStateException.notPending();
        }
    }

    private void requireExists(UUID profileId) {
        if (!hostProfileRepository.existsById(profileId)) {
            throw new HostProfileNotFoundException();
        }
    }

    private static HostVerificationState verificationState(HostProfile p) {
        return new HostVerificationState(p.getId(), p.getOwnerId(), p.getVerificationStatus(), p.getIdType(),
                p.getRejectionReason(), p.getVerificationRequestedAt());
    }

    private HostProfileResponse toResponse(HostProfile profile) {
        List<HostGroupMember> accepted = profile.getType() == HostType.GROUP
                ? memberRepository.findByIdHostProfileIdAndStatusOrderByAcceptedAtAsc(profile.getId(), MemberStatus.ACCEPTED)
                : List.of();
        List<UUID> userIds = new ArrayList<>();
        userIds.add(profile.getOwnerId());
        accepted.forEach(m -> userIds.add(m.getId().userId()));
        // One query for the owner and every member's name.
        Map<UUID, UserSummary> users = userService.summaries(userIds);

        List<HostMemberDto> members = new ArrayList<>();
        members.add(new HostMemberDto(profile.getOwnerId(), nameOf(users, profile.getOwnerId()), true));
        accepted.forEach(m -> members.add(new HostMemberDto(m.getId().userId(), nameOf(users, m.getId().userId()), false)));
        return HostProfileResponse.from(profile, nameOf(users, profile.getOwnerId()), members);
    }

    private static String nameOf(Map<UUID, UserSummary> users, UUID userId) {
        UserSummary user = users.get(userId);
        return user == null ? null : user.displayName();
    }

    private static void validateGroupFields(HostProfileRequest request) {
        if (request.type() != HostType.GROUP) {
            return;
        }
        Map<String, String> errors = new LinkedHashMap<>();
        if (request.groupKind() == null) {
            errors.put("groupKind", "is required for a group");
        }
        if (request.groupName() == null || request.groupName().isBlank()) {
            errors.put("groupName", "is required for a group");
        }
        if (!errors.isEmpty()) {
            throw new InvalidHostProfileException(errors);
        }
    }

    private static String instagramHandle(String raw) {
        String handle = blankToNull(raw);
        return handle != null && handle.startsWith("@") ? handle.substring(1) : handle;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
