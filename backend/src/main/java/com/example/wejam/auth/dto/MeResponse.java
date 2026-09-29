package com.example.wejam.auth.dto;

import com.example.wejam.auth.Role;
import com.example.wejam.auth.User;

import java.util.Set;
import java.util.UUID;

public record MeResponse(UUID id, String phone, String displayName, Set<Role> roles) {

    public static MeResponse from(User user) {
        return new MeResponse(user.getId(), user.getPhone(), user.getDisplayName(), user.getRoles());
    }
}
