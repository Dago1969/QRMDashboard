package com.qtm.dashboard.project.service;

import com.qtm.commonlib.dto.ProjectAdministratorDto;
import com.qtm.dashboard.project.entity.ProjectEntity;
import com.qtm.dashboard.user.entity.RoleEntity;
import com.qtm.dashboard.user.entity.UserRoleProjectEntity;
import com.qtm.dashboard.user.entity.UserRoleProjectId;
import com.qtm.dashboard.user.repository.RoleRepository;
import com.qtm.dashboard.user.repository.UserRoleProjectRepository;
import com.qtm.dashboard.user.service.UserProvisioningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Sincronizza gli amministratori del progetto con la tabella user_role_project e con i ruoli client di Keycloak.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectAdministratorAssignmentService {

    private static final Pattern ADMIN_WORD_PATTERN = Pattern.compile("(^|\\W)admin(\\W|$)");

    private final RoleRepository roleRepository;
    private final UserRoleProjectRepository userRoleProjectRepository;
    private final UserProvisioningService userProvisioningService;

    @Transactional
    public void synchronizeProjectAdministrators(ProjectEntity project) {
        if (project.getId() == null || project.getTenant() == null || project.getTenant().getId() == null) {
            log.warn("[ProjectAdministratorAssignmentService] Progetto senza chiavi persistite, salto la sincronizzazione admin");
            return;
        }

        log.info("[ProjectAdministratorAssignmentService] Avvio sincronizzazione projectId={} tenantId={} clientCode={} administrators={}",
            project.getId(),
            project.getTenant().getId(),
            project.getTenant().getClientCode(),
            Optional.ofNullable(project.getAdministrators()).orElse(List.of()).stream()
                .map(administrator -> administrator == null
                    ? "null"
                    : "{userId=" + administrator.getUserId()
                    + ", roleId=" + administrator.getRoleId()
                    + ", username=" + administrator.getUsername()
                    + ", email=" + administrator.getEmail() + "}")
                .toList());

        Map<String, RoleEntity> rolesById = roleRepository.findAll().stream()
                .filter(Objects::nonNull)
                .filter(role -> normalize(role.getId()) != null)
                .collect(LinkedHashMap::new, (map, role) -> map.put(normalize(role.getId()), role), Map::putAll);
        List<String> allowedAdminRoleIds = resolveAllowedAdminRoleIds();
        log.info("[ProjectAdministratorAssignmentService] Ruoli centralizzati caricati: {}", rolesById.values().stream()
            .map(role -> "{id=" + role.getId() + ", name=" + role.getName() + ", description=" + role.getDescription() + "}")
            .toList());
        log.info("[ProjectAdministratorAssignmentService] allowedAdminRoleIds={}", allowedAdminRoleIds);
        if (allowedAdminRoleIds.isEmpty()) {
            log.warn("[ProjectAdministratorAssignmentService] Nessun ruolo Admin/Admin QTM trovato nella tabella roles; provo fallback sui roleId ricevuti per projectId={}", project.getId());
        }

        Long projectId = project.getId();
        Long tenantId = project.getTenant().getId();
        String clientCode = project.getTenant().getClientCode();

        Map<UserRoleProjectId, UserRoleProjectEntity> existingAssignments = userRoleProjectRepository.findByProjectId(projectId).stream()
                .filter(entity -> Objects.equals(entity.getTenantId(), tenantId))
                .filter(entity -> isAdministrativeRoleId(entity.getRoleId(), rolesById, allowedAdminRoleIds))
                .collect(LinkedHashMap::new, (map, entity) -> map.put(toId(entity), entity), Map::putAll);
        log.info("[ProjectAdministratorAssignmentService] existingAssignments amministrativi per projectId={}: {}",
            projectId,
            existingAssignments.keySet().stream().map(this::describeAssignment).toList());

        Set<UserRoleProjectId> desiredAssignments = buildDesiredAssignments(project, tenantId, allowedAdminRoleIds, rolesById);
        log.info("[ProjectAdministratorAssignmentService] desiredAssignments per projectId={}: {}",
            projectId,
            desiredAssignments.stream().map(this::describeAssignment).toList());

        List<UserRoleProjectEntity> assignmentsToDelete = existingAssignments.entrySet().stream()
                .filter(entry -> !desiredAssignments.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();
        if (!assignmentsToDelete.isEmpty()) {
            log.info("[ProjectAdministratorAssignmentService] assignmentsToDelete per projectId={}: {}",
                projectId,
                assignmentsToDelete.stream().map(entity -> describeAssignment(toId(entity))).toList());
            userRoleProjectRepository.deleteAll(assignmentsToDelete);
        }

        List<UserRoleProjectId> assignmentsToInsert = desiredAssignments.stream()
                .filter(assignmentId -> !existingAssignments.containsKey(assignmentId))
            .toList();
        log.info("[ProjectAdministratorAssignmentService] assignmentsToInsert per projectId={}: {}",
            projectId,
            assignmentsToInsert.stream().map(this::describeAssignment).toList());
        assignmentsToInsert.stream()
            .map(this::toEntity)
            .forEach(userRoleProjectRepository::save);
        if (!assignmentsToInsert.isEmpty()) {
            log.info("[ProjectAdministratorAssignmentService] Persistite {} nuove righe user_role_project per projectId={}",
                assignmentsToInsert.size(),
                projectId);
        }

        synchronizeKeycloakAssignments(desiredAssignments, clientCode);
    }

    private Set<UserRoleProjectId> buildDesiredAssignments(ProjectEntity project,
                                                           Long tenantId,
                                                           List<String> allowedAdminRoleIds,
                                                           Map<String, RoleEntity> rolesById) {
        Set<UserRoleProjectId> desiredAssignments = new LinkedHashSet<>();
        List<ProjectAdministratorDto> administrators = Optional.ofNullable(project.getAdministrators()).orElse(List.of());
        for (ProjectAdministratorDto administrator : administrators) {
            if (administrator == null || administrator.getUserId() == null) {
                continue;
            }

            String resolvedRoleId = resolveAdministratorRoleId(administrator, allowedAdminRoleIds, rolesById);
                log.info("[ProjectAdministratorAssignmentService] Risoluzione admin projectId={} userId={} originalRoleId={} resolvedRoleId={}",
                    project.getId(), administrator.getUserId(), administrator.getRoleId(), resolvedRoleId);
            if (resolvedRoleId == null) {
                log.warn("[ProjectAdministratorAssignmentService] Admin progetto ignorato per userId={} roleId={} sul projectId={} perche' il ruolo non e' Admin/Admin QTM",
                        administrator.getUserId(), administrator.getRoleId(), project.getId());
                continue;
            }

            UserRoleProjectId assignmentId = new UserRoleProjectId();
            assignmentId.setUserId(administrator.getUserId());
            assignmentId.setTenantId(tenantId);
            assignmentId.setRoleId(resolvedRoleId);
            assignmentId.setProjectId(project.getId());
            desiredAssignments.add(assignmentId);
        }
        return desiredAssignments;
    }

    private void synchronizeKeycloakAssignments(Set<UserRoleProjectId> desiredAssignments, String clientCode) {
        if (clientCode == null || clientCode.isBlank()) {
            log.warn("[ProjectAdministratorAssignmentService] Client code mancante: sincronizzazione Keycloak saltata");
            return;
        }

        Map<Long, Set<String>> rolesByUser = new LinkedHashMap<>();
        desiredAssignments.forEach(assignment -> rolesByUser
                .computeIfAbsent(assignment.getUserId(), ignored -> new LinkedHashSet<>())
                .add(assignment.getRoleId()));

        log.info("[ProjectAdministratorAssignmentService] Invocazione sincronizzazione Keycloak: clientCode={} rolesByUser={}",
            clientCode,
            rolesByUser);

        rolesByUser.forEach((userId, roleIds) -> {
            try {
                userProvisioningService.synchronizeExistingUserClientRoles(
                    userId,
                    clientCode,
                    new ArrayList<>(roleIds)
                );
                log.info("[ProjectAdministratorAssignmentService] Sincronizzazione Keycloak completata per userId={} clientCode={} roleIds={}",
                    userId,
                    clientCode,
                    roleIds);
            } catch (Exception exception) {
                log.error("[ProjectAdministratorAssignmentService] Sincronizzazione Keycloak fallita per userId={} clientCode={} roleIds={}. Le righe user_role_project restano persistite.",
                    userId,
                    clientCode,
                    roleIds,
                    exception);
            }
        });
    }

    private List<String> resolveAllowedAdminRoleIds() {
        return roleRepository.findAll().stream()
                .filter(this::isProjectAdministratorRole)
                .map(RoleEntity::getId)
                .distinct()
                .toList();
    }

    private boolean isProjectAdministratorRole(RoleEntity role) {
        String normalized = StreamSupport.joinRoleFields(role);
        return normalized.contains("admin qtm") || ADMIN_WORD_PATTERN.matcher(normalized).find();
    }

    private String resolveAdministratorRoleId(ProjectAdministratorDto administrator,
                                              List<String> allowedAdminRoleIds,
                                              Map<String, RoleEntity> rolesById) {
        String roleId = normalize(administrator.getRoleId());
        if (roleId == null) {
            return null;
        }

        String explicitAllowedRoleId = allowedAdminRoleIds.stream()
                .filter(allowedRoleId -> allowedRoleId.equalsIgnoreCase(roleId))
                .findFirst()
                .orElse(null);
        if (explicitAllowedRoleId != null) {
            return explicitAllowedRoleId;
        }

        RoleEntity roleEntity = rolesById.get(roleId);
        if (roleEntity != null && isProjectAdministratorRole(roleEntity)) {
            return roleEntity.getId();
        }

        return isAdministrativeRoleId(roleId, rolesById, allowedAdminRoleIds) ? administrator.getRoleId().trim() : null;
    }

    private boolean isAdministrativeRoleId(String roleId,
                                           Map<String, RoleEntity> rolesById,
                                           List<String> allowedAdminRoleIds) {
        String normalizedRoleId = normalize(roleId);
        if (normalizedRoleId == null) {
            return false;
        }

        boolean explicitlyAllowed = allowedAdminRoleIds.stream().anyMatch(allowedRoleId -> allowedRoleId.equalsIgnoreCase(normalizedRoleId));
        if (explicitlyAllowed) {
            return true;
        }

        RoleEntity roleEntity = rolesById.get(normalizedRoleId);
        if (roleEntity != null) {
            return isProjectAdministratorRole(roleEntity);
        }

        String normalizedComparisonRoleId = normalizedRoleId.toLowerCase(Locale.ROOT);
        return normalizedComparisonRoleId.contains("admin qtm") || ADMIN_WORD_PATTERN.matcher(normalizedComparisonRoleId).find();
    }

    private String describeAssignment(UserRoleProjectId assignmentId) {
        return "{userId=" + assignmentId.getUserId()
                + ", tenantId=" + assignmentId.getTenantId()
                + ", roleId=" + assignmentId.getRoleId()
                + ", projectId=" + assignmentId.getProjectId() + "}";
    }

    private UserRoleProjectId toId(UserRoleProjectEntity entity) {
        UserRoleProjectId id = new UserRoleProjectId();
        id.setUserId(entity.getUserId());
        id.setTenantId(entity.getTenantId());
        id.setRoleId(entity.getRoleId());
        id.setProjectId(entity.getProjectId());
        return id;
    }

    private UserRoleProjectEntity toEntity(UserRoleProjectId id) {
        UserRoleProjectEntity entity = new UserRoleProjectEntity();
        entity.setUserId(id.getUserId());
        entity.setTenantId(id.getTenantId());
        entity.setRoleId(id.getRoleId());
        entity.setProjectId(id.getProjectId());
        return entity;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static final class StreamSupport {
        private StreamSupport() {
        }

        private static String joinRoleFields(RoleEntity role) {
            return java.util.stream.Stream.of(role.getId(), role.getName(), role.getDescription())
                    .filter(Objects::nonNull)
                    .map(value -> value.toLowerCase(Locale.ROOT))
                    .reduce("", (left, right) -> left + " " + right)
                    .trim();
        }
    }
}