package com.qtm.dashboard.asl.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qtm.commonlib.dto.ASLDto;
import com.qtm.commonlib.dto.ReferentDto;
import com.qtm.dashboard.asl.entity.ASLEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ASLMapper {

    private static final TypeReference<List<ReferentDto>> REFERENT_LIST_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public ASLDto entityToDto(ASLEntity entity) {
        if (entity == null) {
            return null;
        }

        return ASLDto.builder()
                .id(entity.getId())
                .note(entity.getNote())
            .referents(readReferents(entity.getReferentsJson()))
                .build();
    }

    public ASLEntity dtoToEntity(ASLDto dto) {
        if (dto == null) {
            return null;
        }

        return ASLEntity.builder()
                .id(dto.getId())
                .note(dto.getNote())
                .referentsJson(writeReferents(dto.getReferents()))
                .build();
    }

    private List<ReferentDto> readReferents(String referentsJson) {
        if (referentsJson == null || referentsJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(referentsJson, REFERENT_LIST_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Impossibile leggere i referenti ASL", exception);
        }
    }

    private String writeReferents(List<ReferentDto> referents) {
        try {
            return objectMapper.writeValueAsString(referents == null ? List.of() : referents);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Impossibile serializzare i referenti ASL", exception);
        }
    }
}
