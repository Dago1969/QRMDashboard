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
            dto.setTenantCode(entity.getTenant().getClientCode());
            dto.setTenantName(entity.getTenant().getClientName());
        }
        if (entity.getProject() != null) {
            dto.setProjectId(entity.getProject().getId());
            dto.setProjectCode(entity.getProject().getCode());
            dto.setProjectDescription(entity.getProject().getDescrizione());
        }
        return dto;
    }

    public UserTenantProjectRelation toEntity(UserTenantProjectRelationDto dto) {
        UserTenantProjectRelation entity = new UserTenantProjectRelation();
        // Mappo solo i campi semplici, i riferimenti a user, tenant, project saranno settati dal Service
        entity.setEmail(dto.getEmail());
        // Gli id e i riferimenti a user, tenant, project vanno gestiti dal Service
        // Lascio user, tenant, project a null: saranno settati dal Service prima del salvataggio
        return entity;
    }
}
