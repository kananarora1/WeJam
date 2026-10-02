package com.example.wejam.auth.dto;

import java.util.UUID;

/** What other modules may know about a user. Phone is for the owner-side masking only; never expose it raw. */
public record UserSummary(UUID id, String displayName, String phone) {
}
