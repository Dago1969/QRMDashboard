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
public class UserRoleProjectService {

    private final UserRoleProjectRepository repository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantAppPointerRepository tenantAppPointerRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public List<UserRoleProjectDto> findByUserAndTenant(Long userId, Long tenantId) {
        return repository.findByUserIdAndTenantIdOrderByRoleIdAscProjectIdAsc(userId, tenantId).stream()
                .map(this::toDto)
                .toList();
    }

        @Transactional(readOnly = true)
        public List<DashboardUserProjectDto> findDashboardProjectsByUserId(Long userId) {
        Map<Long, TenantAppPointerEntity> tenantsById = tenantAppPointerRepository.findAll().stream()
            .collect(java.util.stream.Collectors.toMap(TenantAppPointerEntity::getId, tenant -> tenant));
        Map<Long, ProjectEntity> projectsById = projectRepository.findAll().stream()
            .collect(java.util.stream.Collectors.toMap(ProjectEntity::getId, project -> project));

        List<DashboardUserProjectDto> relations = repository.findByUserId(userId).stream()
            .map(entity -> toDashboardDto(entity, tenantsById, projectsById))
            .toList();

        if (relations.isEmpty()) {
            return List.of();
        }

        return relations.stream()
            .filter(relation -> relation.getTenantId() != null)
            .collect(java.util.stream.Collectors.groupingBy(
                DashboardUserProjectDto::getTenantId,
                LinkedHashMap::new,
                java.util.stream.Collectors.toList()))
            .values().stream()
            .flatMap(tenantRelations -> collapseTenantProjectsForDashboard(tenantRelations).stream())
            .toList();
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

        Optional.ofNullable(projectsById.get(entity.getProjectId())).ifPresent(project -> {
            dto.setProjectCode(project.getCode());
            dto.setProjectDescription(project.getDescrizione());
        });

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