package com.qtm.dashboard.auth.controller;

import com.qtm.dashboard.auth.dto.ChangePasswordRequest;
import com.qtm.dashboard.auth.dto.LoginRequest;
import com.qtm.dashboard.auth.dto.LoginResponse;
import com.qtm.dashboard.auth.service.KeycloakAuthService;
import com.qtm.dashboard.user.dto.RegisterRequest;
import com.qtm.commonlib.dto.UserDto;
import com.qtm.dashboard.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST per operazioni di accesso: login verso Keycloak e registrazione locale.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final KeycloakAuthService keycloakAuthService;
    private final UserService userService;

    public AuthController(KeycloakAuthService keycloakAuthService, UserService userService) {
        this.keycloakAuthService = keycloakAuthService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(keycloakAuthService.login(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        keycloakAuthService.changePassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.registerUser(request));
    }
}
