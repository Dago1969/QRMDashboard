package com.qtm.dashboard.tenant.mapper;

import com.qtm.dashboard.tenant.dto.TenantAppPointerDto;
import com.qtm.dashboard.tenant.dto.TenantAppPointerUpsertRequestDto;
import com.qtm.dashboard.tenant.entity.TenantAppPointerEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper per conversioni entity/DTO dei puntamenti tenant app.
 */
@Component
public class TenantAppPointerMapper {

    public TenantAppPointerDto toDto(TenantAppPointerEntity entity) {
        return TenantAppPointerDto.builder()
                .id(entity.getId())
                .clientCode(entity.getClientCode())
                .clientName(entity.getClientName())
                .tenantAppUrl(entity.getTenantAppUrl())
                .enabled(entity.isEnabled())
                .build();
    }

    public TenantAppPointerEntity toEntity(TenantAppPointerUpsertRequestDto request) {
        return TenantAppPointerEntity.builder()
                .clientCode(request.getClientCode())
                .clientName(request.getClientName())
                .tenantAppUrl(request.getTenantAppUrl())
                .enabled(Boolean.TRUE.equals(request.getEnabled()))
                .build();
    }

    public void updateEntity(TenantAppPointerEntity entity, TenantAppPointerUpsertRequestDto request) {
        entity.setClientCode(request.getClientCode());
        entity.setClientName(request.getClientName());
        entity.setTenantAppUrl(request.getTenantAppUrl());
        entity.setEnabled(Boolean.TRUE.equals(request.getEnabled()));
    }
}
