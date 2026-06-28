package com.qtm.dashboard.asl.mapper;

import com.qtm.commonlib.dto.ASLDto;
import com.qtm.dashboard.asl.entity.ASLEntity;
import org.springframework.stereotype.Component;

@Component
public class ASLMapper {

    public ASLDto entityToDto(ASLEntity entity) {
        if (entity == null) {
            return null;
        }

        return ASLDto.builder()
                .id(entity.getId())
                .note(entity.getNote())
                .build();
    }

    public ASLEntity dtoToEntity(ASLDto dto) {
        if (dto == null) {
            return null;
        }

        return ASLEntity.builder()
                .id(dto.getId())
                .note(dto.getNote())
                .build();
    }
}
