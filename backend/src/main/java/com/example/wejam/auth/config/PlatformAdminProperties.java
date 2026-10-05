package com.example.wejam.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Phone numbers (E.164) whose users are platform admins. The list is the source of truth: the role is granted
 * at login when a number is listed and revoked when it no longer is. Nothing in the API can create admins.
 */
@ConfigurationProperties("wejam.admin")
public record PlatformAdminProperties(List<String> phones) {

    public PlatformAdminProperties {
        phones = phones == null ? List.of() : phones.stream().map(String::strip).filter(p -> !p.isEmpty()).toList();
    }

    public boolean isAdmin(String phone) {
        return phone != null && phones.contains(phone);
    }
}
