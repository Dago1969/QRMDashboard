package com.qtm.dashboard.auth.service;

import com.qtm.dashboard.auth.dto.LoginRequest;
import com.qtm.dashboard.auth.dto.LoginResponse;
import com.qtm.dashboard.config.KeycloakProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Objects;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Service dedicato all'accesso verso endpoint OIDC token di Keycloak.
 */
@Service
public class KeycloakAuthService {

    private final RestClient restClient;
    private final KeycloakProperties keycloakProperties;

    public KeycloakAuthService(KeycloakProperties keycloakProperties) {
        this.restClient = RestClient.builder().build();
        this.keycloakProperties = keycloakProperties;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", keycloakProperties.getClientId());
        formData.add("username", loginRequest.getUsername());
        formData.add("password", loginRequest.getPassword());
        formData.add("grant_type", keycloakProperties.getGrantType());

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
