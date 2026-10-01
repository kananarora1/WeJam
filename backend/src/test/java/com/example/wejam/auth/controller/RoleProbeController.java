package com.example.wejam.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Test-only endpoint to prove the JWT "roles" claim maps to hasRole(...). */
@RestController
class RoleProbeController {

    @GetMapping("/test/host-only")
    @PreAuthorize("hasRole('HOST')")
    String hostOnly() {
        return "ok";
    }
}
