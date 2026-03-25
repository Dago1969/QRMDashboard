package com.qtm.dashboard.user.service;

import com.qtm.commonlib.dto.UserTenantProjectRelationDto;
import com.qtm.dashboard.user.entity.UserTenantProjectRelation;
import com.qtm.dashboard.user.mapper.UserTenantProjectRelationMapper;
import com.qtm.dashboard.user.repository.UserTenantProjectRelationRepository;
import com.qtm.dashboard.project.entity.ProjectEntity;
import com.qtm.dashboard.project.repository.ProjectRepository;
import com.qtm.dashboard.user.entity.UserEntity;
import com.qtm.dashboard.user.repository.UserRepository;
import com.qtm.dashboard.tenant.entity.TenantAppPointerEntity;
import com.qtm.dashboard.tenant.repository.TenantAppPointerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Service per la gestione della relazione User-Tenant-Project.
 * Gestisce la logica di visibilità (superuser/utente normale).
 */
@Service
public class UserTenantProjectRelationService {
    private static final Logger log = LoggerFactory.getLogger(UserTenantProjectRelationService.class);
    @Autowired
    private UserTenantProjectRelationRepository repository;
    @Autowired
    private UserTenantProjectRelationMapper mapper;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TenantAppPointerRepository tenantAppPointerRepository;

    public List<UserTenantProjectRelationDto> findByUserId(Long userId) {
        log.info("[Service] findByUserId chiamato con userId={}", userId);
        List<UserTenantProjectRelation> list = repository.findByUserIdWithFetch(userId);
        log.info("[Service] findByUserId trovate relazioni: {}", list.size());
        return list.stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Restituisce le relazioni progetto per la dashboard.
     * Se per uno specifico client non esistono righe, oppure l'utente copre tutti i progetti del client,
     * il frontend deve mostrare un box generale e non uno per progetto.
     */
    public List<UserTenantProjectRelationDto> findDashboardProjectsByUserId(Long userId) {
        List<UserTenantProjectRelationDto> relations = findByUserId(userId);

        if (relations.isEmpty()) {
            return List.of();
        }

        Map<Long, List<UserTenantProjectRelationDto>> relationsByTenant = relations.stream()
            .filter(relation -> relation.getTenantId() != null)
            .collect(Collectors.groupingBy(
                UserTenantProjectRelationDto::getTenantId,
                LinkedHashMap::new,
                Collectors.toList()
            ));

        return relationsByTenant.values().stream()
            .flatMap(tenantRelations -> collapseTenantProjectsForDashboard(tenantRelations).stream())
            .toList();
    }

    public List<UserTenantProjectRelationDto> findByTenantId(Long tenantId, boolean onlySuperuser) {
        log.info("[Service] findByTenantId chiamato con tenantId={}, onlySuperuser={}", tenantId, onlySuperuser);
        List<UserTenantProjectRelation> list = repository.findByTenantIdWithFetch(tenantId);
        log.info("[Service] findByTenantId trovate relazioni: {}", list.size());
        return list.stream()
            .filter(rel -> !onlySuperuser || rel.isSuperuser())
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }

    public List<UserTenantProjectRelationDto> findByProjectId(Long projectId) {
        log.info("[Service] findByProjectId chiamato con projectId={}", projectId);
        List<UserTenantProjectRelation> list = repository.findByProjectIdWithFetch(projectId);
        log.info("[Service] findByProjectId trovate relazioni: {}", list.size());
        return list.stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }

    public List<UserTenantProjectRelationDto> findByUserIdAndTenantId(Long userId, Long tenantId) {
        log.info("[Service] findByUserIdAndTenantId chiamato con userId={} tenantId={}", userId, tenantId);
        return repository.findByUserIdAndTenantIdWithFetch(userId, tenantId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserTenantProjectRelationDto save(UserTenantProjectRelationDto dto) {
        log.info("[Service] save chiamato con dto={}", dto);
        UserTenantProjectRelation entity = mapper.toEntity(dto);
        // Risoluzione referenze obbligatorie
        if (dto.getProjectId() == null) {
            throw new IllegalArgumentException("projectId non valorizzato nel DTO!");
        }
        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("userId non valorizzato nel DTO!");
        }
        if (dto.getTenantId() == null) {
            throw new IllegalArgumentException("tenantId non valorizzato nel DTO!");
        }
        ProjectEntity project = projectRepository.findById(dto.getProjectId())
            .orElseThrow(() -> new IllegalArgumentException("Project non trovato per id=" + dto.getProjectId()));
        UserEntity user = userRepository.findById(dto.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User non trovato per id=" + dto.getUserId()));
        TenantAppPointerEntity tenant = tenantAppPointerRepository.findById(dto.getTenantId())
            .orElseThrow(() -> new IllegalArgumentException("Tenant non trovato per id=" + dto.getTenantId()));
        entity.setProject(project);
        entity.setUser(user);
        entity.setTenant(tenant);
        UserTenantProjectRelation saved = repository.save(entity);
        log.info("[Service] save relazione salvata per tenant_id={}, user_id={}, project_id={}",
            (saved.getTenant() != null ? saved.getTenant().getId() : null),
            (saved.getUser() != null ? saved.getUser().getId() : null),
            (saved.getProject() != null ? saved.getProject().getId() : null));
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Long userId, Long tenantId, Long projectId) {
        log.info("[Service] delete chiamato con userId={} tenantId={} projectId={}", userId, tenantId, projectId);
        UserTenantProjectRelation relation = repository.findByUserIdAndTenantIdAndProjectIdWithFetch(userId, tenantId, projectId)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND,
                        "Relazione user-tenant-project non trovata"
                ));
        repository.delete(relation);
    }

    private List<UserTenantProjectRelationDto> collapseTenantProjectsForDashboard(List<UserTenantProjectRelationDto> tenantRelations) {
        Set<Long> assignedProjectIds = tenantRelations.stream()
            .map(UserTenantProjectRelationDto::getProjectId)
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toSet());

        if (assignedProjectIds.isEmpty()) {
            return tenantRelations;
        }

        UserTenantProjectRelationDto firstRelation = tenantRelations.get(0);
        Long tenantId = firstRelation.getTenantId();
        Set<Long> availableProjectIds = projectRepository.findByTenant_Id(tenantId).stream()
            .map(ProjectEntity::getId)
            .collect(Collectors.toSet());

        if (!availableProjectIds.isEmpty() && availableProjectIds.equals(assignedProjectIds)) {
            log.info("[Service] Dashboard tenantId={} consolidato su box generale perche l'utente copre tutti i progetti", tenantId);
            return List.of(buildTenantLevelRelation(firstRelation));
        }

        return tenantRelations;
    }

    private UserTenantProjectRelationDto buildTenantLevelRelation(UserTenantProjectRelationDto source) {
        return UserTenantProjectRelationDto.builder()
            .userId(source.getUserId())
            .username(source.getUsername())
            .tenantId(source.getTenantId())
            .tenantCode(source.getTenantCode())
            .tenantName(source.getTenantName())
            .superuser(source.isSuperuser())
            .email(source.getEmail())
            .build();
    }
}
