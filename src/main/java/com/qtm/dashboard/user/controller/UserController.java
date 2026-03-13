package com.qtm.dashboard.user.controller;

import com.qtm.commonlib.dto.UserDto;
import com.qtm.dashboard.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller REST CRUD utenti centralizzati e dati dashboard.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDto> create(
            @RequestBody UserDto userDto,
            @RequestHeader(name = "X-Selected-Client", required = false) String selectedClient
    ) {
        enrichClientId(userDto, selectedClient);
        return ResponseEntity.ok(userService.create(userDto));
    }

    @GetMapping
    public ResponseEntity<List<com.qtm.commonlib.dto.UserDto>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<com.qtm.commonlib.dto.UserDto>> search(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String roleId,
            @RequestParam(required = false) Long structureId,
            @RequestParam(required = false) Boolean enabled
    ) {
        return ResponseEntity.ok(userService.search(username, email, roleId, structureId, enabled));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(
            @PathVariable Long id,
            @RequestBody UserDto userDto,
            @RequestHeader(name = "X-Selected-Client", required = false) String selectedClient
    ) {
        enrichClientId(userDto, selectedClient);
        return ResponseEntity.ok(userService.update(id, userDto));
    }

    private void enrichClientId(UserDto userDto, String selectedClient) {
        if (userDto == null) {
            return;
        }

        if (userDto.getClientId() != null && !userDto.getClientId().isBlank()) {
            return;
        }

        if (selectedClient != null && !selectedClient.isBlank()) {
            userDto.setClientId(selectedClient.trim());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard(@AuthenticationPrincipal Jwt jwt) {
        Map<String, List<String>> clientRoles = extractClientRoles(jwt);
        Map<String, Object> response = new LinkedHashMap<>();
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        String username = jwt.getClaimAsString("username");
        String sub = jwt.getSubject();
        log.info("[QTMDashboard] JWT subject: {}", sub);
        log.info("[QTMDashboard] JWT preferred_username: {}", preferredUsername);
        log.info("[QTMDashboard] JWT username: {}", username);
        log.info("[QTMDashboard] JWT claims: {}", jwt.getClaims());
        response.put("message", "Accesso dashboard autorizzato");
        response.put("username", preferredUsername);
        response.put("subject", sub);
        response.put("clientRoles", clientRoles);
        response.put("decodedClaims", extractDecodedClaims(jwt));
        return ResponseEntity.ok(response);
    }

        private Map<String, Object> extractDecodedClaims(Jwt jwt) {
        return jwt.getClaims().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (first, second) -> first,
                LinkedHashMap::new
            ));
        }

    @SuppressWarnings("unchecked")
    private Map<String, List<String>> extractClientRoles(Jwt jwt) {
        Object resourceAccessObj = jwt.getClaim("resource_access");
        if (!(resourceAccessObj instanceof Map<?, ?> resourceAccess)) {
            return Map.of();
        }

        return resourceAccess.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof String)
                .collect(Collectors.toMap(
                        entry -> (String) entry.getKey(),
                        entry -> extractRolesFromClientAccess(entry.getValue()),
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRolesFromClientAccess(Object clientAccessObj) {
        if (!(clientAccessObj instanceof Map<?, ?> clientAccessMap)) {
            return List.of();
        }

        Object rolesObj = clientAccessMap.get("roles");
        if (!(rolesObj instanceof List<?> rolesList)) {
            return List.of();
        }

        return rolesList.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList();
    }
}
