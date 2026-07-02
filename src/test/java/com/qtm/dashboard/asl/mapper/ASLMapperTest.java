package com.qtm.dashboard.asl.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qtm.dashboard.asl.entity.ASLEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ASLMapperTest {

    private final ASLMapper aslMapper = new ASLMapper(new ObjectMapper());

    @Test
    void shouldApplyCodiceAziendaAndCodiceRegioneToEntity() {
        ASLEntity entity = new ASLEntity();

        ASLEntity updated = aslMapper.applyCodes(entity, "AZ123", "RM");

        assertEquals("AZ123", updated.getCodiceAzienda());
        assertEquals("RM", updated.getCodiceRegione());
    }
}
