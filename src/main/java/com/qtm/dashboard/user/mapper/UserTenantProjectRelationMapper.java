package com.qtm.dashboard.user.mapper;

import com.qtm.commonlib.dto.UserTenantProjectRelationDto;
import com.qtm.dashboard.user.entity.UserTenantProjectRelation;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper per UserTenantProjectRelation <-> UserTenantProjectRelationDto.
 * Utilizza ModelMapper e gestisce i campi custom.
 */
@Component
public class UserTenantProjectRelationMapper {
    private final ModelMapper modelMapper = new ModelMapper();

    public UserTenantProjectRelationDto toDto(UserTenantProjectRelation entity) {
        UserTenantProjectRelationDto dto = modelMapper.map(entity, UserTenantProjectRelationDto.class);
        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setUsername(entity.getUser().getUsername());
            dto.setEmail(entity.getUser().getEmail());
        }
        if (entity.getTenant() != null) {
            dto.setTenantId(entity.getTenant().getId());
            dto.setTenantName(entity.getTenant().getClientName());
        }
        if (entity.getProject() != null) {
            dto.setProjectId(entity.getProject().getId());
            dto.setProjectCode(entity.getProject().getCode());
        }
        return dto;
    }

    public UserTenantProjectRelation toEntity(UserTenantProjectRelationDto dto) {
        UserTenantProjectRelation entity = modelMapper.map(dto, UserTenantProjectRelation.class);
        entity.setEmail(dto.getEmail());
        // Attenzione: i riferimenti a User, Tenant, Project vanno risolti dal Service
        return entity;
    }
}
