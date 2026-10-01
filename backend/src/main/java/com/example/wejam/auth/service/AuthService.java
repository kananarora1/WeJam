package com.example.wejam.auth.service;

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

    public AuthService(FirebaseTokenVerifier firebaseTokenVerifier, UserRepository userRepository,
                       JwtService jwtService) {
        this.firebaseTokenVerifier = firebaseTokenVerifier;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    /** Verifies the Firebase token, creates the user on first login, and issues an app JWT. */
    @Transactional
    public TokenResponse exchange(String firebaseIdToken) {
        FirebaseIdentity identity = firebaseTokenVerifier.verify(firebaseIdToken);

        boolean created = userRepository.insertIfAbsent(identity.uid(), identity.phoneNumber()) == 1;
        if (created) {
            userRepository.addRoleByFirebaseUid(identity.uid(), Role.USER.name());
        }

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

    private TokenResponse tokenFor(User user) {
        return TokenResponse.bearer(jwtService.issue(user.getId(), user.getRoles()), jwtService.ttlSeconds());
    }
}
