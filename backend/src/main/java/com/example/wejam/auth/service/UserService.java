package com.example.wejam.auth.service;

import com.example.wejam.auth.dto.MeResponse;
import com.example.wejam.auth.dto.UserSummary;
import com.example.wejam.auth.exception.UserNotFoundException;
import com.example.wejam.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public MeResponse getProfile(UUID userId) {
        return userRepository.findWithRolesById(userId)
                .map(MeResponse::from)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional(readOnly = true)
    public Optional<UserSummary> findByPhone(String phoneE164) {
        return userRepository.findSummaryByPhone(phoneE164);
    }

    /** One query for many users (avoids N+1 when listing members). */
    @Transactional(readOnly = true)
    public Map<UUID, UserSummary> summaries(Collection<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findSummariesByIdIn(userIds).stream()
                .collect(Collectors.toMap(UserSummary::id, Function.identity()));
    }

    @Transactional
    public MeResponse updateDisplayName(UUID userId, String displayName) {
        if (userRepository.updateDisplayName(userId, displayName.strip()) == 0) {
            throw new UserNotFoundException(userId);
        }
        return getProfile(userId);
    }
}
