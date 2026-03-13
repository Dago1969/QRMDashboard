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
import org.keycloak.representations.idm.RoleRepresentation;
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
import java.util.LinkedHashSet;
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
        String generatedPassword = generatePassword();

        Optional<UserEntity> existingDbUser = userRepository.findByUsernameIgnoreCase(normalizedUsername);

        Keycloak keycloak = buildAdminClient();
        try {
            RealmResource realmResource = keycloak.realm(requiredRealm());
            ClientRepresentation requestedClient = resolveClient(realmResource, requestedClientId);
            List<String> requestedClientRoleNames = resolveRequestedClientRoleNames(userDto.getRoleId());

            UserResource keycloakUserResource = upsertKeycloakUser(
                    realmResource,
                    userDto,
                    normalizedUsername,
                    requestedClient,
                    requestedClientRoleNames);

            log.info("[UserProvisioningService] Password generata per username={}: {}", normalizedUsername, generatedPassword);
            applyPasswordWithoutRequiredChange(keycloakUserResource, generatedPassword);

                    UserEntity persistedEntity = upsertDatabaseUser(userDto, normalizedUsername, generatedPassword, existingDbUser.orElse(null));

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
                                            ClientRepresentation requestedClient,
                                            List<String> requestedClientRoleNames) {
        String requestedClientId = requestedClient != null ? requestedClient.getClientId() : null;
        UserRepresentation existingUser = findKeycloakUser(realmResource, normalizedUsername);
        if (existingUser == null) {
            String createdUserId = createKeycloakUser(realmResource, userDto, normalizedUsername, requestedClientId);
            UserResource createdUserResource = realmResource.users().get(createdUserId);
            synchronizeClientAssociation(createdUserResource, requestedClientId);
            synchronizeClientRoleAssociation(createdUserResource, requestedClient, requestedClientRoleNames);
            return createdUserResource;
        }

        UserResource existingUserResource = realmResource.users().get(existingUser.getId());
        synchronizeExistingUser(existingUserResource, userDto, normalizedUsername, requestedClientId);
        synchronizeClientRoleAssociation(existingUserResource, requestedClient, requestedClientRoleNames);
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

    /**
     * Assicura un mapping client-level reale su Keycloak, cosi' il client richiesto compare in resource_access.
     */
    private void synchronizeClientRoleAssociation(UserResource userResource,
                                                  ClientRepresentation requestedClient,
                                                  List<String> requestedClientRoleNames) {
        if (requestedClient == null || requestedClient.getId() == null) {
            return;
        }

        List<RoleRepresentation> currentClientRoles = Optional.ofNullable(
                userResource.roles().clientLevel(requestedClient.getId()).listAll())
            .orElse(List.of());

        List<RoleRepresentation> availableRoles = Optional.ofNullable(
                userResource.roles().clientLevel(requestedClient.getId()).listAvailable())
            .orElse(List.of());
        List<RoleRepresentation> rolesToAssign = resolveClientRolesToAssign(
                requestedClient,
                currentClientRoles,
                availableRoles,
                requestedClientRoleNames);
        if (rolesToAssign.isEmpty()) {
            log.info("[UserProvisioningService] Nessun nuovo ruolo client da assegnare per client {} e utente corrente",
                    requestedClient.getClientId());
            return;
        }

        userResource.roles().clientLevel(requestedClient.getId()).add(rolesToAssign);
        log.info("[UserProvisioningService] Associati al client {} i ruoli {}",
                requestedClient.getClientId(),
                rolesToAssign.stream().map(RoleRepresentation::getName).toList());
    }

    private List<RoleRepresentation> resolveClientRolesToAssign(ClientRepresentation requestedClient,
                                                                List<RoleRepresentation> currentClientRoles,
                                                                List<RoleRepresentation> availableRoles,
                                                                List<String> requestedClientRoleNames) {
        List<String> normalizedRequestedRoleNames = Optional.ofNullable(requestedClientRoleNames)
                .orElse(List.of())
                .stream()
                .map(this::normalizeRoleName)
                .filter(Objects::nonNull)
                .toList();
        if (!normalizedRequestedRoleNames.isEmpty()) {
            List<RoleRepresentation> explicitRoles = findMatchingRoles(availableRoles, normalizedRequestedRoleNames);
            if (explicitRoles.isEmpty()) {
                throw new ResponseStatusException(
                        BAD_REQUEST,
                        "Ruolo client Keycloak non trovato per client " + requestedClient.getClientId()
                                + ": richiesto uno tra " + normalizedRequestedRoleNames
                                + ", disponibili " + availableRoles.stream()
                                        .map(RoleRepresentation::getName)
                                        .filter(Objects::nonNull)
                                        .toList());
            }

            return excludeAlreadyAssignedRoles(explicitRoles, currentClientRoles);
        }

        if (availableRoles == null || availableRoles.isEmpty()) {
            return List.of();
        }

        List<String> preferredRoleNames = Stream.concat(
                Stream.of(Optional.ofNullable(requestedClient.getDefaultRoles()).orElse(new String[0])),
                Stream.of("user", "default", "access", requestedClient.getClientId()))
            .filter(Objects::nonNull)
            .map(this::normalizeRoleName)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        List<RoleRepresentation> preferredRoles = findMatchingRoles(availableRoles, preferredRoleNames);
        if (!preferredRoles.isEmpty()) {
            return excludeAlreadyAssignedRoles(preferredRoles, currentClientRoles);
        }

        List<RoleRepresentation> nonPrivilegedRoles = availableRoles.stream()
                .filter(Objects::nonNull)
                .filter(role -> isLikelyAssociationRole(role.getName()))
                .toList();
        if (nonPrivilegedRoles.size() == 1) {
            return excludeAlreadyAssignedRoles(nonPrivilegedRoles, currentClientRoles);
        }

        return List.of();
    }

    private List<String> resolveRequestedClientRoleNames(String roleId) {
        String normalizedRoleId = normalizeNullable(roleId);
        if (normalizedRoleId == null) {
            return List.of();
        }

        RoleEntity requestedRole = findRoleById(normalizedRoleId);
        LinkedHashSet<String> candidateRoleNames = Stream.of(
                    requestedRole.getId(),
                    requestedRole.getName(),
                    requestedRole.getDescription())
                .map(this::normalizeRoleName)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        return List.copyOf(candidateRoleNames);
    }

    private List<RoleRepresentation> findMatchingRoles(List<RoleRepresentation> availableRoles,
                                                       List<String> normalizedRoleNames) {
        if (availableRoles == null || availableRoles.isEmpty() || normalizedRoleNames == null || normalizedRoleNames.isEmpty()) {
            return List.of();
        }

        return availableRoles.stream()
                .filter(Objects::nonNull)
                .filter(role -> normalizedRoleNames.contains(normalizeRoleName(role.getName())))
                .toList();
    }

    private List<RoleRepresentation> excludeAlreadyAssignedRoles(List<RoleRepresentation> candidateRoles,
                                                                 List<RoleRepresentation> currentClientRoles) {
        List<String> currentRoleNames = Optional.ofNullable(currentClientRoles)
                .orElse(List.of())
                .stream()
                .map(RoleRepresentation::getName)
                .map(this::normalizeRoleName)
                .filter(Objects::nonNull)
                .toList();

        return Optional.ofNullable(candidateRoles)
                .orElse(List.of())
                .stream()
                .filter(role -> !currentRoleNames.contains(normalizeRoleName(role.getName())))
                .toList();
    }

    private boolean isLikelyAssociationRole(String roleName) {
        String normalizedRoleName = normalizeRoleName(roleName);
        if (normalizedRoleName == null) {
            return false;
        }

        return !normalizedRoleName.contains("admin")
                && !normalizedRoleName.contains("manage")
                && !normalizedRoleName.contains("owner")
                && !normalizedRoleName.contains("uma");
    }

    private String normalizeRoleName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return null;
        }

        return roleName.trim().toLowerCase(Locale.ROOT);
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

    private UserEntity upsertDatabaseUser(UserDto userDto,
                                          String normalizedUsername,
                                          String generatedPassword,
                                          UserEntity existingDbUser) {
        UserEntity entity = existingDbUser != null ? existingDbUser : userMapper.toEntity(userDto);
        entity.setUsername(normalizedUsername);
        entity.setEnabled(userDto.isEnabled());
        entity.setEmail(normalizeNullable(userDto.getEmail()));
        entity.setStructureId(userDto.getStructureId());
        entity.setRole(findRoleById(userDto.getRoleId()));
        entity.setPasswordHash(generatedPassword);
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
