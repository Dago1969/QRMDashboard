package com.qtm.dashboard.user.service;

import com.qtm.commonlib.dto.UserRoleProjectDto;
import com.qtm.dashboard.project.entity.ProjectEntity;
import com.qtm.dashboard.project.repository.ProjectRepository;
import com.qtm.dashboard.tenant.entity.TenantAppPointerEntity;
import com.qtm.dashboard.tenant.repository.TenantAppPointerRepository;
import com.qtm.dashboard.user.dto.DashboardUserProjectDto;
import com.qtm.dashboard.user.entity.RoleEntity;
import com.qtm.dashboard.user.entity.UserEntity;
import com.qtm.dashboard.user.entity.UserRoleProjectEntity;
import com.qtm.dashboard.user.repository.RoleRepository;
import com.qtm.dashboard.user.repository.UserRepository;
import com.qtm.dashboard.user.repository.UserRoleProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Service centralizzato per le associazioni utente-tenant-ruolo-progetto.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleProjectService {

    private final UserRoleProjectRepository repository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantAppPointerRepository tenantAppPointerRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public List<UserRoleProjectDto> findByUserAndTenant(Long userId, Long tenantId) {
        List<UserRoleProjectEntity> entities = repository.findByUserIdAndTenantIdOrderByRoleIdAscProjectIdAsc(userId, tenantId);
        log.info("[UserRoleProjectService] findByUserAndTenant userId={} tenantId={} rows={} rowsData={}",
                userId,
                tenantId,
                entities.size(),
                entities);
        return entities.stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DashboardUserProjectDto> findDashboardProjectsByUserId(Long userId) {
        log.info("[UserRoleProjectService] findDashboardProjectsByUserId start userId={}", userId);
        Map<Long, TenantAppPointerEntity> tenantsById = tenantAppPointerRepository.findAll().stream()
            .collect(java.util.stream.Collectors.toMap(TenantAppPointerEntity::getId, tenant -> tenant));
        Map<Long, ProjectEntity> projectsById = projectRepository.findAll().stream()
            .collect(java.util.stream.Collectors.toMap(ProjectEntity::getId, project -> project));

        List<UserRoleProjectEntity> userRoleProjectRows = repository.findByUserId(userId);
        log.info("[UserRoleProjectService] user_role_project rows for userId={} count={} rowsData={}",
                userId,
                userRoleProjectRows.size(),
                userRoleProjectRows);

        List<DashboardUserProjectDto> relations = userRoleProjectRows.stream()
            .map(entity -> toDashboardDto(entity, tenantsById, projectsById))
            .toList();

        log.info("[UserRoleProjectService] mapped dashboard relations for userId={} count={} relations={}",
                userId,
                relations.size(),
                relations);

        if (relations.isEmpty()) {
            log.warn("[UserRoleProjectService] no dashboard relations for userId={}", userId);
            return List.of();
        }

        List<DashboardUserProjectDto> collapsed = relations.stream()
            .filter(relation -> relation.getTenantId() != null)
            .collect(java.util.stream.Collectors.groupingBy(
                DashboardUserProjectDto::getTenantId,
                LinkedHashMap::new,
                java.util.stream.Collectors.toList()))
            .values().stream()
            .flatMap(tenantRelations -> collapseTenantProjectsForDashboard(tenantRelations).stream())
            .toList();

        log.info("[UserRoleProjectService] collapsed dashboard relations for userId={} count={} collapsed={}",
                userId,
                collapsed.size(),
                collapsed);
        return collapsed;
    }

    @Transactional
    public UserRoleProjectDto save(UserRoleProjectDto dto) {
        findUser(dto.getUserId());
        findRole(dto.getRoleId());
        findTenant(dto.getTenantId());

        if (dto.getProjectId() == null) {
            throw new ResponseStatusException(NOT_FOUND, "Progetto non valorizzato");
        }

        UserRoleProjectEntity entity = new UserRoleProjectEntity();
        entity.setUserId(dto.getUserId());
        entity.setTenantId(dto.getTenantId());
        entity.setRoleId(dto.getRoleId());
        entity.setProjectId(dto.getProjectId());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(Long userId, Long tenantId, String roleId, Long projectId) {
        repository.deleteByUserIdAndTenantIdAndRoleIdAndProjectId(userId, tenantId, roleId, projectId);
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        repository.deleteByUserId(userId);
    }

    private UserRoleProjectDto toDto(UserRoleProjectEntity entity) {
        UserRoleProjectDto dto = new UserRoleProjectDto();
        dto.setUserId(entity.getUserId());
        dto.setTenantId(entity.getTenantId());
        dto.setRoleId(entity.getRoleId());
        dto.setProjectId(entity.getProjectId());
        return dto;
    }

    private DashboardUserProjectDto toDashboardDto(UserRoleProjectEntity entity,
                                                   Map<Long, TenantAppPointerEntity> tenantsById,
                                                   Map<Long, ProjectEntity> projectsById) {
        DashboardUserProjectDto dto = new DashboardUserProjectDto();
        dto.setUserId(entity.getUserId());
        dto.setTenantId(entity.getTenantId());
        dto.setProjectId(entity.getProjectId());
        dto.setRoleId(entity.getRoleId());
        dto.setSuperuser(false);

        Optional.ofNullable(tenantsById.get(entity.getTenantId())).ifPresent(tenant -> {
            dto.setTenantCode(tenant.getClientCode());
            dto.setTenantName(tenant.getClientName());
        });
        if (dto.getTenantCode() == null) {
            log.warn("[UserRoleProjectService] missing tenant mapping for tenantId={} in user_role_project row userId={} roleId={} projectId={}",
                    entity.getTenantId(),
                    entity.getUserId(),
                    entity.getRoleId(),
                    entity.getProjectId());
        }

        Optional.ofNullable(projectsById.get(entity.getProjectId())).ifPresent(project -> {
            dto.setProjectCode(project.getCode());
            dto.setProjectDescription(project.getDescrizione());
        });
        if (dto.getProjectCode() == null) {
            log.warn("[UserRoleProjectService] missing project mapping for projectId={} in user_role_project row userId={} tenantId={} roleId={}",
                    entity.getProjectId(),
                    entity.getUserId(),
                    entity.getTenantId(),
                    entity.getRoleId());
        }

        return dto;
    }

    private List<DashboardUserProjectDto> collapseTenantProjectsForDashboard(List<DashboardUserProjectDto> tenantRelations) {
        Map<String, DashboardUserProjectDto> uniqueRoleProject = tenantRelations.stream()
                .filter(dto -> dto.getRoleId() != null && dto.getProjectId() != null)
                .collect(java.util.stream.Collectors.toMap(
                        dto -> dto.getRoleId() + "_" + dto.getProjectId(),
                        dto -> dto,
                        (left, right) -> left,
                        LinkedHashMap::new));

        return uniqueRoleProject.isEmpty() ? tenantRelations : uniqueRoleProject.values().stream().toList();
    }

    private UserEntity findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Utente non trovato"));
    }

    private RoleEntity findRole(String roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Ruolo non trovato"));
    }

    private void findTenant(Long tenantId) {
        tenantAppPointerRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tenant non trovato"));
    }
}