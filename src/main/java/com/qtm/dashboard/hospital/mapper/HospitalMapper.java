package com.qtm.dashboard.hospital.mapper;

import com.qtm.commonlib.dto.HospitalDto;
import com.qtm.dashboard.hospital.entity.HospitalEntity;
import org.springframework.stereotype.Component;

@Component
public class HospitalMapper {

    public HospitalDto entityToDto(HospitalEntity entity) {
        if (entity == null) {
            return null;
        }
        return HospitalDto.builder()
                .id(entity.getId())
                .build();
    }

    public HospitalEntity dtoToEntity(HospitalDto dto) {
        if (dto == null) {
            return null;
        }
        return HospitalEntity.builder()
                .id(dto.getId())
                .build();
    }
}
