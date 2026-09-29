package com.example.wejam.auth.dto;

import com.example.wejam.auth.Role;
import jakarta.validation.constraints.NotNull;

public record AddRoleRequest(@NotNull Role role) {
}
