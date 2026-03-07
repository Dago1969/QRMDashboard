package com.qtm.dashboard.user.mapper;

import com.qtm.dashboard.user.dto.RoleDto;
import com.qtm.dashboard.user.entity.RoleEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper dedicato alla conversione RoleEntity <-> RoleDto.
 */
@Component
public class RoleMapper {

    public RoleDto toDto(RoleEntity entity) {
        RoleDto dto = new RoleDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }
}
