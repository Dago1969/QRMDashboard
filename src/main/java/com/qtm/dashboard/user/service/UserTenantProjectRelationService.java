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

import java.util.List;
import java.util.stream.Collectors;

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
}
