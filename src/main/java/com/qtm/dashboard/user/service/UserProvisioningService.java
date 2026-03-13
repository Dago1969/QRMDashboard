package com.qtm.dashboard.user.service;

import com.qtm.commonlib.dto.UserDto;
import com.qtm.dashboard.config.KeycloakProperties;
import com.qtm.dashboard.user.entity.RoleEntity;
import com.qtm.dashboard.user.entity.UserEntity;
import com.qtm.dashboard.user.mapper.UserMapper;
import com.qtm.dashboard.user.repository.RoleRepository;
import com.qtm.dashboard.user.repository.UserRepository;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Service dedicato al provisioning utente su Keycloak e sul DB locale.
 * Riceve un UserDto, verifica l'utente su DB e Keycloak, associa il client richiesto,
 * genera una password definitiva, la applica su Keycloak e inserisce l'utente nel DB se assente.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProvisioningService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final KeycloakProperties keycloakProperties;

    @Transactional
    public UserDto provisionUser(UserDto userDto) {
        String normalizedUsername = normalizeRequired(userDto.getUsername(), "Username obbligatorio");
        String requestedClientId = resolveRequestedClientId(userDto);

        Optional<UserEntity> existingDbUser = userRepository.findByUsernameIgnoreCase(normalizedUsername);

        Keycloak keycloak = buildAdminClient();
        try {
            RealmResource realmResource = keycloak.realm(requiredRealm());
            resolveClient(realmResource, requestedClientId);

            UserResource keycloakUserResource = upsertKeycloakUser(realmResource, userDto, normalizedUsername, requestedClientId);

            String generatedPassword = generatePassword();
            log.info("[UserProvisioningService] Password generata per username={}: {}", normalizedUsername, generatedPassword);
            applyPasswordWithoutRequiredChange(keycloakUserResource, generatedPassword);

            UserEntity persistedEntity = existingDbUser.orElseGet(() -> insertIntoDatabase(userDto, normalizedUsername));

            UserDto result = userMapper.toDto(persistedEntity);
            result.setClientId(requestedClientId);
            result.setPassword(generatedPassword);
            return result;
        } finally {
            keycloak.close();
        }
    }

    private String generatePassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[12];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private Keycloak buildAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(normalizeRequired(keycloakProperties.getServerUrl(), "Server URL Keycloak mancante"))
                .realm(requiredRealm())
                .grantType(normalizeRequired(keycloakProperties.getAdminGrantType(), "Grant type admin Keycloak mancante"))
                .clientId(normalizeRequired(keycloakProperties.getAdminClientId(), "Admin clientId Keycloak mancante"))
                .clientSecret(normalizeRequired(keycloakProperties.getAdminClientSecret(), "Admin clientSecret Keycloak mancante"))
                .build();
    }

    private String requiredRealm() {
        return normalizeRequired(keycloakProperties.getRealm(), "Realm Keycloak mancante");
    }

    private String resolveRequestedClientId(UserDto userDto) {
        String dtoClientId = normalizeNullable(userDto.getClientId());
        if (dtoClientId != null) {
            return dtoClientId;
        }

        return normalizeNullable(keycloakProperties.getClientId());
    }

    private ClientRepresentation resolveClient(RealmResource realmResource, String clientId) {
        if (clientId == null) {
            return null;
        }

        return realmResource.clients().findByClientId(clientId).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Client Keycloak non trovato: " + clientId));
    }

    private UserResource upsertKeycloakUser(RealmResource realmResource,
                                            UserDto userDto,
                                            String normalizedUsername,
                                            String requestedClientId) {
        UserRepresentation existingUser = findKeycloakUser(realmResource, normalizedUsername);
        if (existingUser == null) {
            String createdUserId = createKeycloakUser(realmResource, userDto, normalizedUsername, requestedClientId);
            UserResource createdUserResource = realmResource.users().get(createdUserId);
            synchronizeClientAssociation(createdUserResource, requestedClientId);
            return createdUserResource;
        }

        UserResource existingUserResource = realmResource.users().get(existingUser.getId());
        synchronizeExistingUser(existingUserResource, userDto, normalizedUsername, requestedClientId);
        return existingUserResource;
    }

    private UserRepresentation findKeycloakUser(RealmResource realmResource, String username) {
        return Stream.of(
                realmResource.users().searchByUsername(username, true).stream(),
                realmResource.users().search(username, true).stream(),
                realmResource.users().search(username, 0, 20).stream())
            .flatMap(stream -> stream)
            .filter(Objects::nonNull)
            .filter(user -> user.getUsername() != null && user.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    private String createKeycloakUser(RealmResource realmResource,
                                      UserDto userDto,
                                      String normalizedUsername,
                                      String requestedClientId) {
        UserRepresentation representation = new UserRepresentation();
        representation.setUsername(normalizedUsername);
        representation.setEmail(normalizeNullable(userDto.getEmail()));
        representation.setEnabled(Boolean.TRUE.equals(userDto.isEnabled()) || userDto.isEnabled());
        representation.setRequiredActions(new ArrayList<>());
        representation.setAttributes(buildAssociationAttributes(requestedClientId));

        try (Response response = realmResource.users().create(representation)) {
            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                return extractCreatedId(response);
            }

            if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                UserRepresentation existing = findKeycloakUser(realmResource, normalizedUsername);
                if (existing != null) {
                    log.info("[UserProvisioningService] Utente Keycloak gia' esistente per username={}, riutilizzo id={}",
                            normalizedUsername, existing.getId());
                    return existing.getId();
                }
            }

            throw new ResponseStatusException(BAD_REQUEST,
                    "Impossibile creare l'utente su Keycloak. HTTP status=" + response.getStatus()
                            + formatKeycloakErrorDetails(response));
        }
    }

    private String formatKeycloakErrorDetails(Response response) {
        String responseBody = response.readEntity(String.class);
        if (responseBody == null || responseBody.isBlank()) {
            return "";
        }

        return ", dettagli=" + responseBody;
    }

    private void synchronizeExistingUser(UserResource userResource,
                                         UserDto userDto,
                                         String normalizedUsername,
                                         String requestedClientId) {
        UserRepresentation representation = userResource.toRepresentation();
        boolean changed = false;

        if (!normalizedUsername.equalsIgnoreCase(Objects.toString(representation.getUsername(), ""))) {
            representation.setUsername(normalizedUsername);
            changed = true;
        }

        String normalizedEmail = normalizeNullable(userDto.getEmail());
        if (normalizedEmail != null && !normalizedEmail.equalsIgnoreCase(Objects.toString(representation.getEmail(), ""))) {
            representation.setEmail(normalizedEmail);
            changed = true;
        }

        if (representation.isEnabled() == null || representation.isEnabled() != userDto.isEnabled()) {
            representation.setEnabled(userDto.isEnabled());
            changed = true;
        }

        Map<String, List<String>> mergedAttributes = mergeClientAssociation(representation.getAttributes(), requestedClientId);
        if (!Objects.equals(mergedAttributes, representation.getAttributes())) {
            representation.setAttributes(mergedAttributes);
            changed = true;
        }

        if (changed) {
            userResource.update(representation);
        }
    }

    private void synchronizeClientAssociation(UserResource userResource, String requestedClientId) {
        if (requestedClientId == null) {
            return;
        }

        UserRepresentation representation = userResource.toRepresentation();
        Map<String, List<String>> mergedAttributes = mergeClientAssociation(representation.getAttributes(), requestedClientId);
        if (!Objects.equals(mergedAttributes, representation.getAttributes())) {
            representation.setAttributes(mergedAttributes);
            userResource.update(representation);
        }
    }

    private void applyPasswordWithoutRequiredChange(UserResource userResource, String generatedPassword) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(generatedPassword);
        credentialRepresentation.setTemporary(false);
        userResource.resetPassword(credentialRepresentation);

        UserRepresentation representation = userResource.toRepresentation();
        List<String> requiredActions = new ArrayList<>(Optional.ofNullable(representation.getRequiredActions()).orElse(List.of()));
        if (requiredActions.removeIf(action -> "UPDATE_PASSWORD".equalsIgnoreCase(action))) {
            representation.setRequiredActions(requiredActions);
            userResource.update(representation);
        }
    }

    private UserEntity insertIntoDatabase(UserDto userDto, String normalizedUsername) {
        UserEntity entity = userMapper.toEntity(userDto);
        entity.setUsername(normalizedUsername);
        entity.setRole(findRoleById(userDto.getRoleId()));
        return userRepository.save(entity);
    }

    private RoleEntity findRoleById(String roleId) {
        String normalizedRoleId = normalizeNullable(roleId);
        if (normalizedRoleId == null) {
            return null;
        }

        return roleRepository.findById(normalizedRoleId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Ruolo non trovato: " + normalizedRoleId));
    }

    private Map<String, List<String>> buildAssociationAttributes(String requestedClientId) {
        if (requestedClientId == null) {
            return null;
        }

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(clientAssociationAttributeKey(), new ArrayList<>(List.of(requestedClientId)));
        return attributes;
    }

    private Map<String, List<String>> mergeClientAssociation(Map<String, List<String>> currentAttributes, String requestedClientId) {
        if (requestedClientId == null) {
            return currentAttributes;
        }

        Map<String, List<String>> merged = new HashMap<>();
        if (currentAttributes != null) {
            currentAttributes.forEach((key, value) -> merged.put(key, value == null ? new ArrayList<>() : new ArrayList<>(value)));
        }

        List<String> associatedClients = merged.computeIfAbsent(clientAssociationAttributeKey(), ignored -> new ArrayList<>());
        boolean alreadyPresent = associatedClients.stream().anyMatch(value -> value.equalsIgnoreCase(requestedClientId));
        if (!alreadyPresent) {
            associatedClients.add(requestedClientId);
            log.info("[UserProvisioningService] Associazione client {} aggiunta all'utente Keycloak", requestedClientId);
        }
        return merged;
    }

    private String clientAssociationAttributeKey() {
        String configured = normalizeNullable(keycloakProperties.getClientAssociationAttribute());
        return configured != null ? configured : "qtm-client-id";
    }

    private String extractCreatedId(Response response) {
        if (response.getLocation() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Keycloak non ha restituito l'id dell'utente creato");
        }

        String path = response.getLocation().getPath();
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            throw new ResponseStatusException(BAD_REQUEST, message);
        }
        return normalized;
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        return normalized.toLowerCase(Locale.ROOT).equals(normalized) ? normalized : value.trim();
    }
}
