package com.example.wejam.auth.service;

import com.example.wejam.auth.config.PlatformAdminProperties;
import com.example.wejam.auth.dto.TokenResponse;
import com.example.wejam.auth.exception.RoleNotSelfAssignableException;
import com.example.wejam.auth.exception.UserNotFoundException;
import com.example.wejam.auth.firebase.FirebaseIdentity;
import com.example.wejam.auth.firebase.FirebaseTokenVerifier;
import com.example.wejam.auth.model.Role;
import com.example.wejam.auth.model.User;
import com.example.wejam.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final FirebaseTokenVerifier firebaseTokenVerifier;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PlatformAdminProperties platformAdmins;

    public AuthService(FirebaseTokenVerifier firebaseTokenVerifier, UserRepository userRepository,
                       JwtService jwtService, PlatformAdminProperties platformAdmins) {
        this.firebaseTokenVerifier = firebaseTokenVerifier;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.platformAdmins = platformAdmins;
    }

    /** Verifies the Firebase token, creates the user on first login, and issues an app JWT. */
    @Transactional
    public TokenResponse exchange(String firebaseIdToken) {
        FirebaseIdentity identity = firebaseTokenVerifier.verify(firebaseIdToken);

        if (identity.phoneNumber() != null) {
            userRepository.relinkPhoneToFirebaseUid(identity.phoneNumber(), identity.uid());
        }
        boolean created = userRepository.insertIfAbsent(identity.uid(), identity.phoneNumber()) == 1;
        if (created) {
            userRepository.addRoleByFirebaseUid(identity.uid(), Role.USER.name());
        }
        syncPlatformAdminRole(identity);

        User user = userRepository.findWithRolesByFirebaseUid(identity.uid()).orElseThrow();
        return tokenFor(user);
    }

    /** Adds a self-assignable role and returns a fresh JWT, since roles in the old token are frozen. */
    @Transactional
    public TokenResponse addRole(UUID userId, Role role) {
        if (!role.isSelfAssignable()) {
            throw new RoleNotSelfAssignableException(role);
        }
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        userRepository.addRole(userId, role.name());

        User user = userRepository.findWithRolesById(userId).orElseThrow();
        return tokenFor(user);
    }

    /** The configured allow-list decides; both statements are idempotent. */
    private void syncPlatformAdminRole(FirebaseIdentity identity) {
        if (platformAdmins.isAdmin(identity.phoneNumber())) {
            userRepository.addRoleByFirebaseUid(identity.uid(), Role.PLATFORM_ADMIN.name());
        } else {
            userRepository.removeRoleByFirebaseUid(identity.uid(), Role.PLATFORM_ADMIN.name());
        }
    }

    private TokenResponse tokenFor(User user) {
        return TokenResponse.bearer(jwtService.issue(user.getId(), user.getRoles()), jwtService.ttlSeconds());
    }
}
