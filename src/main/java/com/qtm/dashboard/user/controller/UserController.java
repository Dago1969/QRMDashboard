package com.qtm.dashboard.user.controller;

import org.springframework.web.server.ResponseStatusException;


import com.qtm.commonlib.dto.UserDto;
import com.qtm.dashboard.project.repository.ProjectRepository;
import com.qtm.dashboard.tenant.repository.TenantAppPointerRepository;
import com.qtm.dashboard.user.service.UserRoleProjectService;
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

import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Controller REST CRUD utenti centralizzati e dati dashboard.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final UserRoleProjectService userRoleProjectService;
    private final TenantAppPointerRepository tenantPointerRepository;
    private final ProjectRepository projectRepository;
    private final com.qtm.dashboard.otp.service.UserOtpService userOtpService;

    public UserController(UserService userService,
                          UserRoleProjectService userRoleProjectService,
                          TenantAppPointerRepository tenantAppPointerRepository,
                          ProjectRepository projectRepository,
                          com.qtm.dashboard.otp.service.UserOtpService userOtpService) {
        this.userService = userService;
        this.userRoleProjectService = userRoleProjectService;
        this.tenantPointerRepository = tenantAppPointerRepository;
        this.projectRepository = projectRepository;
        this.userOtpService = userOtpService;
    }

    @PostMapping
    public ResponseEntity<UserDto> create(
            @RequestBody UserDto userDto,
            @RequestHeader(name = "X-Selected-Client", required = false) String selectedClient,
            @RequestHeader(name = "X-Selected-Project", required = false) String selectedProject
    ) {
        enrichSelectionContext(userDto, selectedClient, selectedProject);
        log.info("[UserController] Creazione utente richiesta username={}, email={}, roleId={}, clientId={}, selectedProject={}",
            userDto.getUsername(), userDto.getEmail(), userDto.getRoleId(), userDto.getClientId(), selectedProject);
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
            @RequestHeader(name = "X-Selected-Client", required = false) String selectedClient,
            @RequestHeader(name = "X-Selected-Project", required = false) String selectedProject
    ) {
        enrichSelectionContext(userDto, selectedClient, selectedProject);
        return ResponseEntity.ok(userService.update(id, userDto));
    }

    private void enrichSelectionContext(UserDto userDto, String selectedClient, String selectedProject) {
        if (userDto == null) {
            return;
        }

        if ((userDto.getClientId() == null || userDto.getClientId().isBlank())
                && selectedClient != null
                && !selectedClient.isBlank()) {
            userDto.setClientId(selectedClient.trim());
        }

        if (userDto.getProjectId() == null && selectedProject != null && !selectedProject.isBlank()) {
            userDto.setProjectId(resolveProjectId(userDto.getClientId(), selectedProject));
        }
    }

    private Long resolveProjectId(String clientId, String selectedProject) {
        if (clientId == null || clientId.isBlank()) {
            return null;
        }

        Long tenantId = tenantPointerRepository.findByClientCode(clientId.trim())
                .map(tenant -> tenant.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tenant non trovato per client: " + clientId.trim()));

        String normalizedSelectedProject = selectedProject.trim();
        try {
            long parsedProjectId = Long.parseLong(normalizedSelectedProject);
            return projectRepository.findById(parsedProjectId)
                    .filter(project -> project.getTenant() != null && tenantId.equals(project.getTenant().getId()))
                    .map(project -> project.getId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Progetto non trovato: " + normalizedSelectedProject));
        } catch (NumberFormatException ignored) {
            return projectRepository.findByCodeIgnoreCaseAndTenant_Id(normalizedSelectedProject, tenantId)
                    .map(project -> project.getId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Progetto non trovato: " + normalizedSelectedProject));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    

    @PostMapping("/otp/send")
    public ResponseEntity<com.qtm.dashboard.otp.dto.OtpVerificationResultResponse> sendOtpByPhone(
            @Valid @RequestBody com.qtm.dashboard.otp.dto.OtpPhoneVerificationSendRequest request
    ) {
        return ResponseEntity.ok(userOtpService.sendOtpForPhone(request));
    }

    @PostMapping("/otp/check")
    public ResponseEntity<com.qtm.dashboard.otp.dto.OtpVerificationResultResponse> checkOtpByPhone(
            @Valid @RequestBody com.qtm.dashboard.otp.dto.OtpPhoneVerificationCheckRequest request
    ) {
        return ResponseEntity.ok(userOtpService.checkOtpForPhone(request));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> response = new LinkedHashMap<>();
        String subject = jwt != null ? jwt.getSubject() : null;
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        Object resourceAccess = jwt != null ? jwt.getClaim("resource_access") : null;
        log.info("[QTMDashboard][dashboard] start subject={} preferredUsername={} resourceAccessKeys={}",
                subject,
                preferredUsername,
                extractResourceAccessKeys(resourceAccess));
        response.put("message", "Accesso dashboard autorizzato");
        response.put("username", preferredUsername);
        response.put("subject", subject);
        response.put("decodedClaims", jwt != null ? jwt.getClaims() : Map.of());
        Long userId = null;
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            try {
                userId = userService.findEntityByUsername(preferredUsername).getId();
                log.info("[QTMDashboard][dashboard] resolved preferredUsername={} to userId={}", preferredUsername, userId);
            } catch (ResponseStatusException ex) {
                log.warn("[QTMDashboard] Utente non trovato per preferred_username: {}", preferredUsername);
            }
        }
        if (userId != null) {
            var projects = userRoleProjectService.findDashboardProjectsByUserId(userId);
            log.info("[QTMDashboard][dashboard] userId={} dashboardProjectsCount={} dashboardProjects={}",
                    userId,
                    projects.size(),
                    projects);
            response.put("userProjects", projects);
        } else {
            log.warn("[QTMDashboard][dashboard] no userId resolved for preferredUsername={}, returning empty userProjects", preferredUsername);
            response.put("userProjects", List.of());
        }
        return ResponseEntity.ok(response);
    }

    private List<String> extractResourceAccessKeys(Object resourceAccess) {
        if (!(resourceAccess instanceof Map<?, ?> resourceAccessMap)) {
            return List.of();
        }

        return resourceAccessMap.keySet().stream()
                .map(String::valueOf)
                .toList();
    }



}
