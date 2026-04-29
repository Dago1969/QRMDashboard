package com.qtm.dashboard.project.repository;

import com.qtm.dashboard.project.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository CRUD dei progetti centralizzati su QTMDashboard.
 */
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    List<ProjectEntity> findByTenant_Id(Long tenantId);

    Optional<ProjectEntity> findByCodeIgnoreCaseAndTenant_Id(String code, Long tenantId);

    boolean existsByCodeIgnoreCaseAndTenant_Id(String code, Long tenantId);

    boolean existsByCodeIgnoreCaseAndTenant_IdAndIdNot(String code, Long tenantId, Long id);
}