package com.example.wejam.host.service;

import com.example.wejam.auth.service.UserService;
import com.example.wejam.host.dto.HostProfileRequest;
import com.example.wejam.host.dto.HostProfileResponse;
import com.example.wejam.host.exception.HostProfileNotFoundException;
import com.example.wejam.host.exception.InvalidHostProfileException;
import com.example.wejam.host.model.HostProfile;
import com.example.wejam.host.model.HostType;
import com.example.wejam.host.model.MediaLink;
import com.example.wejam.host.repository.HostProfileRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class HostProfileService {

    private final HostProfileRepository hostProfileRepository;
    private final UserService userService;

    public HostProfileService(HostProfileRepository hostProfileRepository, UserService userService) {
        this.hostProfileRepository = hostProfileRepository;
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

    private HostProfileResponse toResponse(HostProfile profile) {
        String ownerName = profile.getType() == HostType.INDIVIDUAL
                ? userService.getProfile(profile.getOwnerId()).displayName()
                : null;
        return HostProfileResponse.from(profile, ownerName);
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
