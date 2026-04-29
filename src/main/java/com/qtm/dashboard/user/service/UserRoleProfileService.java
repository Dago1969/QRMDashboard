package com.qtm.dashboard.user.service;

import com.qtm.dashboard.project.repository.ProjectRepository;
import com.qtm.dashboard.tenant.repository.TenantAppPointerRepository;
import com.qtm.dashboard.user.entity.UserRoleProfileEntity;
import com.qtm.dashboard.user.entity.UserRoleProfileId;
import com.qtm.dashboard.user.repository.UserRoleProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Mantiene allineata la tabella user_role_profile con il provisioning utente.
 */
@Service
@RequiredArgsConstructor
public class UserRoleProfileService {

    private final UserRoleProfileRepository userRoleProfileRepository;
    private final TenantAppPointerRepository tenantAppPointerRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public void saveForUser(Long userId, String clientId, String roleId, Long projectId) {
        String normalizedClientId = normalize(clientId);
        String normalizedRoleId = normalize(roleId);
        if (userId == null || normalizedClientId == null || normalizedRoleId == null || projectId == null) {
            return;
        }

        Long tenantId = tenantAppPointerRepository.findByClientCode(normalizedClientId)
                .map(tenant -> tenant.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tenant non trovato per client: " + normalizedClientId));

        boolean projectExistsForTenant = projectRepository.findById(projectId)
                .filter(project -> project.getTenant() != null)
                .map(project -> tenantId.equals(project.getTenant().getId()))
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profilo progetto non trovato: " + projectId));

        if (!projectExistsForTenant) {
            throw new ResponseStatusException(NOT_FOUND, "Profilo progetto non coerente con il tenant selezionato: " + projectId);
        }

        UserRoleProfileId id = new UserRoleProfileId(userId, tenantId, normalizedRoleId, projectId);
        if (userRoleProfileRepository.existsById(id)) {
            return;
        }

        UserRoleProfileEntity entity = new UserRoleProfileEntity();
        entity.setUserId(userId);
        entity.setTenantId(tenantId);
        entity.setRoleId(normalizedRoleId);
        entity.setProfileId(projectId);
        userRoleProfileRepository.save(entity);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}