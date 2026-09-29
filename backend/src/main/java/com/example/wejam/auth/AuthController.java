package com.example.wejam.auth;

import com.example.wejam.auth.dto.TokenRequest;
import com.example.wejam.auth.dto.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
class AuthController {

    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/token")
    TokenResponse exchange(@Valid @RequestBody TokenRequest request) {
        return authService.exchange(request.firebaseIdToken());
    }
}
