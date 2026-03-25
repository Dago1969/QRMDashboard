package com.qtm.dashboard.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qtm.commonlib.dto.UserTenantProjectRelationDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test di accettazione per UserTenantProjectRelationController.
 * Verifica endpoint REST e logica filtro superuser.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserTenantProjectRelationControllerIT {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/user-tenant-project - crea relazione")
    void testCreateRelation() throws Exception {
        UserTenantProjectRelationDto dto = UserTenantProjectRelationDto.builder()
                .userId(1L)
                .tenantId(1L)
                .projectId(null)
                .superuser(true)
                .build();
        mockMvc.perform(post("/api/user-tenant-project")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.tenantId").value(1L))
                .andExpect(jsonPath("$.superuser").value(true));
    }

    @Test
    @DisplayName("GET /api/user-tenant-project/user/{userId} - lista relazioni per utente")
    void testGetByUser() throws Exception {
        mockMvc.perform(get("/api/user-tenant-project/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/user-tenant-project/tenant/{tenantId}?onlySuperuser=true - lista superuser per tenant")
    void testGetByTenantSuperuser() throws Exception {
        mockMvc.perform(get("/api/user-tenant-project/tenant/1?onlySuperuser=true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/user-tenant-project/project/{projectId} - lista relazioni per progetto")
    void testGetByProject() throws Exception {
        mockMvc.perform(get("/api/user-tenant-project/project/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
