package com.qtm.dashboard.user.service;

import com.qtm.commonlib.dto.UserTenantProjectRelationDto;
import com.qtm.dashboard.user.entity.UserTenantProjectRelation;
import com.qtm.dashboard.user.entity.UserRoleProjectEntity;
import com.qtm.dashboard.user.mapper.UserTenantProjectRelationMapper;
import com.qtm.dashboard.user.repository.UserRoleProjectRepository;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service per la gestione della relazione User-Tenant-Project.
 * Gestisce la logica di visibilità (superuser/utente normale).
 */
@Service
public class UserTenantProjectRelationService {
    private static final Logger log = LoggerFactory.getLogger(UserTenantProjectRelationService.class);
    @Autowired
    private UserRoleProjectRepository repository;
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
        List<UserRoleProjectEntity> list = repository.findByUserId(userId);
        log.info("[Service] findByUserId trovate relazioni: {}", list.size());
        List<UserTenantProjectRelationDto> dtos = list.stream()
            .map(entity -> {
                UserTenantProjectRelationDto dto = new UserTenantProjectRelationDto();
                dto.setUserId(entity.getUserId());
                dto.setTenantId(entity.getTenantId());
                dto.setProjectId(entity.getProjectId());
                dto.setRoleId(entity.getRoleId());

                // Popola tenant info
                if (entity.getTenantId() != null) {
                    tenantAppPointerRepository.findById(entity.getTenantId()).ifPresent(tenant -> {
                        dto.setTenantCode(tenant.getClientCode());
                        dto.setTenantName(tenant.getClientName());
                    });
                }

                // Popola project info
                if (entity.getProjectId() != null) {
                    projectRepository.findById(entity.getProjectId()).ifPresent(project -> {
                        dto.setProjectCode(project.getCode());
                        dto.setProjectDescription(project.getDescrizione());
                    });
                }

                // Qui puoi valorizzare altri campi se necessario
                return dto;
            })
            .collect(Collectors.toList());
        for (int i = 0; i < dtos.size(); i++) {
            UserTenantProjectRelationDto dto = dtos.get(i);
            log.info("[BOX {}] tenantId: {} | tenantCode: {} | tenantName: {} | projectId: {} | projectCode: {} | projectDescription: {} | roleId: {} | username: {} | email: {}",
                i, dto.getTenantId(), dto.getTenantCode(), dto.getTenantName(), dto.getProjectId(), dto.getProjectCode(), dto.getProjectDescription(), dto.getRoleId(), dto.getUsername(), dto.getEmail());
        }
        return dtos;
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
        List<UserRoleProjectEntity> list = repository.findByTenantId(tenantId);
        log.info("[Service] findByTenantId trovate relazioni: {}", list.size());
        return list.stream()
            .map(entity -> {
                UserTenantProjectRelationDto dto = new UserTenantProjectRelationDto();
                dto.setUserId(entity.getUserId());
                dto.setTenantId(entity.getTenantId());
                dto.setProjectId(entity.getProjectId());
                dto.setRoleId(entity.getRoleId());
                return dto;
            })
            .collect(Collectors.toList());
    }

    public List<UserTenantProjectRelationDto> findByProjectId(Long projectId) {
        log.info("[Service] findByProjectId chiamato con projectId={}", projectId);
        List<UserRoleProjectEntity> list = repository.findByProjectId(projectId);
        log.info("[Service] findByProjectId trovate relazioni: {}", list.size());
        return list.stream()
            .map(entity -> {
                UserTenantProjectRelationDto dto = new UserTenantProjectRelationDto();
                dto.setUserId(entity.getUserId());
                dto.setTenantId(entity.getTenantId());
                dto.setProjectId(entity.getProjectId());
                dto.setRoleId(entity.getRoleId());
                return dto;
            })
            .collect(Collectors.toList());
    }

    public List<UserTenantProjectRelationDto> findByUserIdAndTenantId(Long userId, Long tenantId) {
        log.info("[Service] findByUserIdAndTenantId chiamato con userId={} tenantId={}", userId, tenantId);
        return repository.findByUserIdAndTenantIdOrderByRoleIdAscProjectIdAsc(userId, tenantId).stream()
            .map(entity -> {
                UserTenantProjectRelationDto dto = new UserTenantProjectRelationDto();
                dto.setUserId(entity.getUserId());
                dto.setTenantId(entity.getTenantId());
                dto.setProjectId(entity.getProjectId());
                dto.setRoleId(entity.getRoleId());
                return dto;
            })
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
        // Conversione da UserTenantProjectRelationDto a UserRoleProjectEntity
        UserRoleProjectEntity newEntity = new UserRoleProjectEntity();
        newEntity.setUserId(dto.getUserId());
        newEntity.setTenantId(dto.getTenantId());
        newEntity.setProjectId(dto.getProjectId());
        newEntity.setRoleId(dto.getRoleId());
        UserRoleProjectEntity saved = repository.save(newEntity);
        log.info("[Service] save relazione salvata per tenant_id={}, user_id={}, project_id={}",
            saved.getTenantId(), saved.getUserId(), saved.getProjectId());
        UserTenantProjectRelationDto result = new UserTenantProjectRelationDto();
        result.setUserId(saved.getUserId());
        result.setTenantId(saved.getTenantId());
        result.setProjectId(saved.getProjectId());
        result.setRoleId(saved.getRoleId());
        return result;
    }

    @Transactional
    public void delete(Long userId, Long tenantId, Long projectId) {
        log.info("[Service] delete chiamato con userId={} tenantId={} projectId={}", userId, tenantId, projectId);
        // In UserRoleProjectRepository serve anche roleId, qui si assume null o da ricavare
        // Se non disponibile, va adattata la logica
        // repository.deleteByUserIdAndTenantIdAndRoleIdAndProjectId(userId, tenantId, roleId, projectId);
        // Per ora, non implementato senza roleId
        throw new UnsupportedOperationException("delete richiede roleId per UserRoleProjectRepository");
    }

    private List<UserTenantProjectRelationDto> collapseTenantProjectsForDashboard(List<UserTenantProjectRelationDto> tenantRelations) {
        // Raggruppa per (roleId, projectId) unici
        List<UserTenantProjectRelationDto> uniqueRoleProject = tenantRelations.stream()
            .filter(dto -> dto.getRoleId() != null && dto.getProjectId() != null)
            .collect(Collectors.collectingAndThen(
                Collectors.toMap(
                    dto -> dto.getRoleId() + "_" + dto.getProjectId(),
                    dto -> dto,
                    (dto1, dto2) -> dto1 // in caso di duplicati, tiene il primo
                ),
                m -> m.values().stream().toList()
            ));

        // Se non ci sono coppie ruolo-progetto, restituisci comunque tenantRelations
        if (uniqueRoleProject.isEmpty()) {
            return tenantRelations;
        }

        return uniqueRoleProject;
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
