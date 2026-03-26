package com.qtm.dashboard.user.controller;

import org.springframework.web.server.ResponseStatusException;


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

/**
 * Controller REST CRUD utenti centralizzati e dati dashboard.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final com.qtm.dashboard.user.service.UserTenantProjectRelationService userTenantProjectRelationService;

    public UserController(UserService userService, com.qtm.dashboard.user.service.UserTenantProjectRelationService userTenantProjectRelationService) {
        this.userService = userService;
        this.userTenantProjectRelationService = userTenantProjectRelationService;
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
        Map<String, Object> response = new LinkedHashMap<>();
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        response.put("message", "Accesso dashboard autorizzato");
        response.put("username", preferredUsername);
        Long userId = null;
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            try {
                userId = userService.findEntityByUsername(preferredUsername).getId();
            } catch (ResponseStatusException ex) {
                log.warn("[QTMDashboard] Utente non trovato per preferred_username: {}", preferredUsername);
            }
        }
        if (userId != null) {
            var projects = userTenantProjectRelationService.findDashboardProjectsByUserId(userId);
            response.put("userProjects", projects);
        } else {
            response.put("userProjects", List.of());
        }
        return ResponseEntity.ok(response);
    }



}
