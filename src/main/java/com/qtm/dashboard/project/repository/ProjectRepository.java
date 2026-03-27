package com.qtm.dashboard.project.repository;

import com.qtm.dashboard.project.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository CRUD dei progetti centralizzati su QTMDashboard.
 */
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    List<ProjectEntity> findByTenant_Id(Long tenantId);

    boolean existsByCodeIgnoreCaseAndTenant_Id(String code, Long tenantId);

    boolean existsByCodeIgnoreCaseAndTenant_IdAndIdNot(String code, Long tenantId, Long id);
}