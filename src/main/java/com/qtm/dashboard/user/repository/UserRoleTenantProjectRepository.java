package com.qtm.dashboard.user.repository;

import com.qtm.dashboard.user.entity.UserRoleTenantProjectEntity;
import com.qtm.dashboard.user.entity.UserRoleTenantProjectId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA per user_role_tenant_project.
 */
@Repository
public interface UserRoleTenantProjectRepository extends JpaRepository<UserRoleTenantProjectEntity, UserRoleTenantProjectId> {

    List<UserRoleTenantProjectEntity> findByUserIdAndTenantIdAndRoleIdOrderByProjectIdAsc(Long userId, Long tenantId, String roleId);

    void deleteByUserIdAndTenantIdAndRoleIdAndProjectId(Long userId, Long tenantId, String roleId, String projectId);
}
