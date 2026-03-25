package com.qtm.dashboard.auth.service;

import com.qtm.dashboard.auth.dto.LoginRequest;
import com.qtm.dashboard.auth.dto.LoginResponse;
import com.qtm.dashboard.config.KeycloakProperties;
import com.qtm.dashboard.user.entity.UserEntity;
import com.qtm.dashboard.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Service dedicato all'accesso verso endpoint OIDC token di Keycloak.
 */
@Service
@Slf4j
public class KeycloakAuthService {

    private final RestClient restClient;
    private final KeycloakProperties keycloakProperties;
    private final UserRepository userRepository;

    public KeycloakAuthService(KeycloakProperties keycloakProperties,
                               UserRepository userRepository) {
        this.restClient = RestClient.builder().build();
        this.keycloakProperties = keycloakProperties;
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        List<String> loginIdentifiers = resolveLoginIdentifiers(loginRequest.getUsername());
        log.info("[KeycloakAuthService] Tentativo login per identificativo={}, candidati={}",
                loginRequest.getUsername(), loginIdentifiers);
        ResponseStatusException lastFailure = null;

        for (String loginIdentifier : loginIdentifiers) {
            try {
                LoginResponse response = loginWithIdentifier(loginIdentifier, loginRequest.getPassword());
                log.info("[KeycloakAuthService] Login Keycloak riuscito con identificativo={}", loginIdentifier);
                return response;
            } catch (ResponseStatusException ex) {
                log.warn("[KeycloakAuthService] Login Keycloak fallito con identificativo={}", loginIdentifier);
                lastFailure = ex;
            }
        }

        if (lastFailure != null) {
            throw lastFailure;
        }

        throw new ResponseStatusException(UNAUTHORIZED, "Credenziali non valide");
    }

    List<String> resolveLoginIdentifiers(String rawLoginIdentifier) {
        String normalizedIdentifier = normalizeLoginIdentifier(rawLoginIdentifier);
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(normalizedIdentifier);

        resolveCanonicalUsername(userRepository.findByUsernameIgnoreCase(normalizedIdentifier))
                .ifPresent(candidates::add);
        resolveCanonicalUsername(userRepository.findByEmailIgnoreCase(normalizedIdentifier))
                .ifPresent(candidates::add);

        return new ArrayList<>(candidates);
    }

    private Optional<String> resolveCanonicalUsername(Optional<UserEntity> userEntity) {
        return userEntity
                .map(UserEntity::getUsername)
                .map(this::normalizeLoginIdentifier);
    }

    private String normalizeLoginIdentifier(String loginIdentifier) {
        String normalizedIdentifier = Objects.requireNonNull(loginIdentifier, "Username obbligatorio").trim();
        if (normalizedIdentifier.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Username obbligatorio");
        }
        return normalizedIdentifier;
    }

    private LoginResponse loginWithIdentifier(String loginIdentifier, String password) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", keycloakProperties.getClientId());
        formData.add("grant_type", keycloakProperties.getGrantType());
        if (keycloakProperties.getClientSecret() != null && !keycloakProperties.getClientSecret().isBlank()) {
            formData.add("client_secret", keycloakProperties.getClientSecret());
        }
        // Solo per grant_type password aggiungi username/password
        if ("password".equalsIgnoreCase(keycloakProperties.getGrantType())) {
            formData.add("username", loginIdentifier);
            formData.add("password", password);
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> keycloakResponse = restClient.post()
                    .uri(Objects.requireNonNull(keycloakProperties.getTokenUrl(), "Token URL Keycloak mancante"))
                    .contentType(Objects.requireNonNull(MediaType.APPLICATION_FORM_URLENCODED))
                    .body(formData)
                    .retrieve()
                    .body(Map.class);

            if (keycloakResponse == null || !keycloakResponse.containsKey("access_token")) {
                throw new ResponseStatusException(BAD_REQUEST, "Risposta non valida da Keycloak");
            }

            return mapToLoginResponse(keycloakResponse);
        } catch (HttpStatusCodeException ex) {
            throw new ResponseStatusException(UNAUTHORIZED, "Credenziali non valide", ex);
        }
    }

    private LoginResponse mapToLoginResponse(Map<String, Object> keycloakResponse) {
        LoginResponse response = new LoginResponse();
        response.setAccessToken(String.valueOf(keycloakResponse.getOrDefault("access_token", "")));
        response.setRefreshToken(String.valueOf(keycloakResponse.getOrDefault("refresh_token", "")));
        response.setTokenType(String.valueOf(keycloakResponse.getOrDefault("token_type", "Bearer")));
        response.setExpiresIn(Long.parseLong(String.valueOf(keycloakResponse.getOrDefault("expires_in", "0"))));
        response.setRefreshExpiresIn(Long.parseLong(String.valueOf(keycloakResponse.getOrDefault("refresh_expires_in", "0"))));
        return response;
    }
}
