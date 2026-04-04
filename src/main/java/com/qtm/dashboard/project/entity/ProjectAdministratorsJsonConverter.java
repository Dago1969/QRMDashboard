package com.qtm.dashboard.project.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qtm.commonlib.dto.ProjectAdministratorDto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter JPA per serializzare gli amministratori di progetto in JSON.
 */
@Converter
public class ProjectAdministratorsJsonConverter implements AttributeConverter<List<ProjectAdministratorDto>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<ProjectAdministratorDto>> TYPE_REFERENCE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(List<ProjectAdministratorDto> attribute) {
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute == null ? List.of() : attribute);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Impossibile serializzare gli amministratori del progetto", exception);
        }
    }

    @Override
    public List<ProjectAdministratorDto> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ArrayList<>();
        }

        try {
            return new ArrayList<>(OBJECT_MAPPER.readValue(dbData, TYPE_REFERENCE));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Impossibile deserializzare gli amministratori del progetto", exception);
        }
    }
}