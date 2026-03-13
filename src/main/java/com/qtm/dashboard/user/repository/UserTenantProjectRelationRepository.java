package com.qtm.dashboard.user.repository;

import com.qtm.dashboard.user.entity.UserTenantProjectRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository per la relazione User-Tenant-Project.
 */
@Repository
public interface UserTenantProjectRelationRepository extends JpaRepository<UserTenantProjectRelation, Long> {
    List<UserTenantProjectRelation> findByUserId(Long userId);
    List<UserTenantProjectRelation> findByTenantId(Long tenantId);
    List<UserTenantProjectRelation> findByProjectId(Long projectId);
}
