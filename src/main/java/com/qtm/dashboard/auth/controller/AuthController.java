package com.qtm.dashboard.auth.controller;

import com.qtm.dashboard.auth.dto.ChangePasswordRequest;
import com.qtm.dashboard.auth.dto.LoginRequest;
import com.qtm.dashboard.auth.dto.LoginResponse;
import com.qtm.dashboard.auth.dto.PasswordRecoverRequest;
import com.qtm.dashboard.auth.dto.PasswordRecoverResponse;
import com.qtm.dashboard.auth.dto.ResetPasswordRequest;
import com.qtm.dashboard.auth.service.KeycloakAuthService;
import com.qtm.dashboard.user.dto.RegisterRequest;
import com.qtm.commonlib.dto.UserDto;
import com.qtm.dashboard.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Controller REST per operazioni di accesso: login verso Keycloak e registrazione locale.
 */
@Slf4j
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
        log.info("Ricevuta richiesta change-password per username={}, currentPasswordLength={}, newPasswordLength={}, confirmPasswordLength={}",
                request.getUsername(),
                request.getCurrentPassword() != null ? request.getCurrentPassword().length() : 0,
                request.getNewPassword() != null ? request.getNewPassword().length() : 0,
                request.getConfirmPassword() != null ? request.getConfirmPassword().length() : 0);
        keycloakAuthService.changePassword(request);
        log.info("Completata richiesta change-password per username={} con esito=SUCCESS", request.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.registerUser(request));
    }

    /**
     * Endpoint per richiesta recupero password: invia mail con link di reset se l'utente esiste.
     * Risposta sempre generica per sicurezza.
     */
    @PostMapping("/passwordrecover")
    public ResponseEntity<PasswordRecoverResponse> passwordRecover(@Valid @RequestBody PasswordRecoverRequest request) {
        log.info("Ricevuta richiesta password recovery per email={}", request.getEmail());
        userService.handlePasswordRecover(request.getEmail());
        return ResponseEntity.ok(new PasswordRecoverResponse("Se l'email è presente riceverai un messaggio con le istruzioni di reset."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Ricevuta richiesta reset-password con tokenLength={}, newPasswordLength={}",
            request.getToken() != null ? request.getToken().length() : 0,
            request.getNewPassword() != null ? request.getNewPassword().length() : 0);
        keycloakAuthService.resetPassword(request);
        log.info("Completata richiesta reset-password con tokenLength={} ed esito=SUCCESS",
            request.getToken() != null ? request.getToken().length() : 0);
        return ResponseEntity.noContent().build();
    }
}
