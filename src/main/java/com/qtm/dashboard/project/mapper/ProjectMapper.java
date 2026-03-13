package com.qtm.dashboard.project.mapper;

import com.qtm.commonlib.dto.ProjectDto;
import com.qtm.dashboard.project.entity.ProjectEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper entity/DTO dei progetti centralizzati.
 */
@Component
public class ProjectMapper {

    public ProjectDto toDto(ProjectEntity entity) {
        ProjectDto dto = new ProjectDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setTenantId(entity.getTenant().getId());
        dto.setTenant(entity.getTenant().getClientName());
        dto.setDescrizione(entity.getDescrizione());
        dto.setDataInizio(entity.getDataInizio());
        dto.setDataFine(entity.getDataFine());
        return dto;
    }
    
    public ProjectEntity toEntity(ProjectDto dto) {
        ProjectEntity entity = new ProjectEntity();
        entity.setId(dto.getId());
        entity.setCode(dto.getCode());
        // tenant va gestito dal service
        entity.setDescrizione(dto.getDescrizione());
        entity.setDataInizio(dto.getDataInizio());
        entity.setDataFine(dto.getDataFine());
        return entity;
    }
}