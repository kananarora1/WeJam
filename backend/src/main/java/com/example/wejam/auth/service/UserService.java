package com.example.wejam.auth.service;

import com.example.wejam.auth.dto.MeResponse;
import com.example.wejam.auth.exception.UserNotFoundException;
import com.example.wejam.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

    @Transactional
    public MeResponse updateDisplayName(UUID userId, String displayName) {
        if (userRepository.updateDisplayName(userId, displayName.strip()) == 0) {
            throw new UserNotFoundException(userId);
        }
        return getProfile(userId);
    }
}
