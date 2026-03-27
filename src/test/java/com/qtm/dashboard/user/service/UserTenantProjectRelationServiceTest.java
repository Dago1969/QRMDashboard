package com.qtm.dashboard.user.service;

import com.qtm.commonlib.dto.UserTenantProjectRelationDto;
import com.qtm.dashboard.project.entity.ProjectEntity;
import com.qtm.dashboard.project.repository.ProjectRepository;
import com.qtm.dashboard.tenant.repository.TenantAppPointerRepository;
import com.qtm.dashboard.user.entity.UserTenantProjectRelation;
import com.qtm.dashboard.user.mapper.UserTenantProjectRelationMapper;
import com.qtm.dashboard.user.repository.UserRepository;
import com.qtm.dashboard.user.repository.UserTenantProjectRelationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * Verifica che la dashboard mostri box per progetto solo quando la copertura utente-cliente e parziale.
 */
@ExtendWith(MockitoExtension.class)
class UserTenantProjectRelationServiceTest {

    @Mock
    private UserTenantProjectRelationRepository repository;

    @Mock
    private UserTenantProjectRelationMapper mapper;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantAppPointerRepository tenantAppPointerRepository;

    @InjectMocks
    private UserTenantProjectRelationService service;

    @Test
    void findDashboardProjectsByUserIdShouldReturnEmptyWhenNoUserTenantProjectRowsExist() {
        when(repository.findByUserIdWithFetch(10L)).thenReturn(List.of());

        List<UserTenantProjectRelationDto> result = service.findDashboardProjectsByUserId(10L);

        assertEquals(List.of(), result);
    }

    @Test
    void findDashboardProjectsByUserIdShouldCollapseProjectsWhenUserCoversAllClientProjects() {
        UserTenantProjectRelation relationOne = new UserTenantProjectRelation();
        UserTenantProjectRelation relationTwo = new UserTenantProjectRelation();
        UserTenantProjectRelationDto projectOne = projectRelation(10L, 20L, 100L, "CLIENT_A", "Project A");
        UserTenantProjectRelationDto projectTwo = projectRelation(10L, 20L, 200L, "CLIENT_A", "Project B");

        when(repository.findByUserIdWithFetch(10L)).thenReturn(List.of(relationOne, relationTwo));
        when(mapper.toDto(relationOne)).thenReturn(projectOne);
        when(mapper.toDto(relationTwo)).thenReturn(projectTwo);
        when(projectRepository.findByTenant_Id(20L)).thenReturn(List.of(projectEntity(100L), projectEntity(200L)));

        List<UserTenantProjectRelationDto> result = service.findDashboardProjectsByUserId(10L);

        assertEquals(1, result.size());
        assertEquals(20L, result.get(0).getTenantId());
        assertEquals("CLIENT_A", result.get(0).getTenantCode());
        assertNull(result.get(0).getProjectId());
        assertNull(result.get(0).getProjectCode());
    }

    @Test
    void findDashboardProjectsByUserIdShouldKeepProjectBoxesWhenCoverageIsPartial() {
        UserTenantProjectRelation relation = new UserTenantProjectRelation();
        UserTenantProjectRelationDto projectOne = projectRelation(10L, 20L, 100L, "CLIENT_A", "Project A");

        when(repository.findByUserIdWithFetch(10L)).thenReturn(List.of(relation));
        when(mapper.toDto(relation)).thenReturn(projectOne);
        when(projectRepository.findByTenant_Id(20L)).thenReturn(List.of(projectEntity(100L), projectEntity(200L)));

        List<UserTenantProjectRelationDto> result = service.findDashboardProjectsByUserId(10L);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getProjectId());
        assertEquals("PRJ-100", result.get(0).getProjectCode());
    }

    private UserTenantProjectRelationDto projectRelation(Long userId, Long tenantId, Long projectId, String tenantCode, String projectDescription) {
        return UserTenantProjectRelationDto.builder()
            .userId(userId)
            .username("utente.test")
            .tenantId(tenantId)
            .tenantCode(tenantCode)
            .tenantName("Client A")
            .projectId(projectId)
            .projectCode("PRJ-" + projectId)
            .projectDescription(projectDescription)
            .superuser(false)
            .email("utente.test@example.com")
            .build();
    }

    private ProjectEntity projectEntity(Long id) {
        ProjectEntity entity = new ProjectEntity();
        entity.setId(id);
        return entity;
    }
}