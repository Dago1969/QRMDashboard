package com.qtm.dashboard.user.repository;

import com.qtm.dashboard.user.entity.UserRoleTenantProjectEntity;
import com.qtm.dashboard.user.entity.UserRoleProjectId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA per user_role_tenant_project.
 */
@Repository
public interface UserRoleTenantProjectRepository extends JpaRepository<UserRoleTenantProjectEntity, UserRoleProjectId> {
    List<UserRoleTenantProjectEntity> findByUserIdAndTenantIdOrderByRoleIdAscProjectIdAsc(Long userId, Long tenantId);

    void deleteByUserIdAndTenantIdAndRoleIdAndProjectId(Long userId, Long tenantId, String roleId, String projectId);
}
