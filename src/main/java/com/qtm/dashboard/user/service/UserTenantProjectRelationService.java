package com.qtm.dashboard.user.service;

import com.qtm.commonlib.dto.UserTenantProjectRelationDto;
import com.qtm.dashboard.user.entity.UserTenantProjectRelation;
import com.qtm.dashboard.user.mapper.UserTenantProjectRelationMapper;
import com.qtm.dashboard.user.repository.UserTenantProjectRelationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service per la gestione della relazione User-Tenant-Project.
 * Gestisce la logica di visibilità (superuser/utente normale).
 */
@Service
public class UserTenantProjectRelationService {
    @Autowired
    private UserTenantProjectRelationRepository repository;
    @Autowired
    private UserTenantProjectRelationMapper mapper;

    public List<UserTenantProjectRelationDto> findByUserId(Long userId) {
        System.out.println("[Service] findByUserId chiamato con userId=" + userId);
        List<UserTenantProjectRelation> list = repository.findByUserId(userId);
        System.out.println("[Service] findByUserId trovate relazioni: " + list.size());
        return list.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public List<UserTenantProjectRelationDto> findByTenantId(Long tenantId, boolean onlySuperuser) {
        System.out.println("[Service] findByTenantId chiamato con tenantId=" + tenantId + ", onlySuperuser=" + onlySuperuser);
        List<UserTenantProjectRelation> list = repository.findByTenantId(tenantId);
        System.out.println("[Service] findByTenantId trovate relazioni: " + list.size());
        return list.stream()
                .filter(rel -> !onlySuperuser || rel.isSuperuser())
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public List<UserTenantProjectRelationDto> findByProjectId(Long projectId) {
        System.out.println("[Service] findByProjectId chiamato con projectId=" + projectId);
        List<UserTenantProjectRelation> list = repository.findByProjectId(projectId);
        System.out.println("[Service] findByProjectId trovate relazioni: " + list.size());
        return list.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public UserTenantProjectRelationDto save(UserTenantProjectRelationDto dto) {
        System.out.println("[Service] save chiamato con dto=" + dto);
        UserTenantProjectRelation entity = mapper.toEntity(dto);
        // Risoluzione referenze User, Tenant, Project va fatta nel Service (omessa per brevità)
        UserTenantProjectRelation saved = repository.save(entity);
        System.out.println("[Service] save relazione salvata con id=" + saved.getId());
        return mapper.toDto(saved);
    }
}
