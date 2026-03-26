
package com.qtm.dashboard.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qtm.commonlib.dto.UserRoleTenantProjectDto;
import com.qtm.dashboard.tenant.repository.TenantAppPointerRepository;
import com.qtm.dashboard.user.entity.RoleEntity;
import com.qtm.dashboard.user.entity.UserEntity;
import com.qtm.dashboard.user.entity.UserRoleTenantProjectEntity;
import com.qtm.dashboard.user.repository.RoleRepository;
import com.qtm.dashboard.user.repository.UserRepository;
import com.qtm.dashboard.user.repository.UserRoleTenantProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Service centralizzato per le associazioni utente-tenant-ruolo-progetto (UserRoleTenantProject).
 */
@Service
@RequiredArgsConstructor
public class UserRoleTenantProjectService {

    private static final Logger log = LoggerFactory.getLogger(UserRoleTenantProjectService.class);

    private final UserRoleTenantProjectRepository repository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantAppPointerRepository tenantAppPointerRepository;

    @Transactional(readOnly = true)
    public List<UserRoleTenantProjectDto> findByUserTenantAndRole(Long userId, Long tenantId, String roleId) {
        log.debug("[UserRoleTenantProjectService] findByUserTenantAndRole userId={} tenantId={} roleId={}", userId, tenantId, roleId);
        return repository.findByUserIdAndTenantIdAndRoleIdOrderByProjectIdAsc(userId, tenantId, roleId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public UserRoleTenantProjectDto save(UserRoleTenantProjectDto dto) {
        findUser(dto.getUserId());
        findRole(dto.getRoleId());
        findTenant(dto.getTenantId());

        if (dto.getProjectId() == null || dto.getProjectId().isBlank()) {
            throw new ResponseStatusException(NOT_FOUND, "Progetto non valorizzato");
        }

        UserRoleTenantProjectEntity entity = new UserRoleTenantProjectEntity();
        entity.setUserId(dto.getUserId());
        entity.setTenantId(dto.getTenantId());
        entity.setRoleId(dto.getRoleId());
        entity.setProjectId(dto.getProjectId().trim());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(Long userId, Long tenantId, String roleId, String projectId) {
        repository.deleteByUserIdAndTenantIdAndRoleIdAndProjectId(userId, tenantId, roleId, projectId);
    }

    private UserRoleTenantProjectDto toDto(UserRoleTenantProjectEntity entity) {
        UserRoleTenantProjectDto dto = new UserRoleTenantProjectDto();
        dto.setUserId(entity.getUserId());
        dto.setTenantId(entity.getTenantId());
        dto.setRoleId(entity.getRoleId());
        dto.setProjectId(entity.getProjectId());
        return dto;
    }

    private UserEntity findUser(Long userId) {
        return userRepository.findById(Objects.requireNonNull(userId, "userId is required"))
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Utente non trovato"));
    }

    private RoleEntity findRole(String roleId) {
        return roleRepository.findById(Objects.requireNonNull(roleId, "roleId is required"))
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Ruolo non trovato"));
    }

    private void findTenant(Long tenantId) {
        tenantAppPointerRepository.findById(Objects.requireNonNull(tenantId, "tenantId is required"))
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tenant non trovato"));
    }
}
