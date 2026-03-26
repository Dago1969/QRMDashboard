package com.qtm.dashboard.user.repository;

import com.qtm.dashboard.user.entity.UserRoleProjectEntity;
import com.qtm.dashboard.user.entity.UserRoleProjectId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA per user_role_project.
 */
@Repository
public interface UserRoleProjectRepository extends JpaRepository<UserRoleProjectEntity, UserRoleProjectId> {

    List<UserRoleProjectEntity> findByUserIdAndTenantIdOrderByRoleIdAscProjectIdAsc(Long userId, Long tenantId);

    void deleteByUserIdAndTenantIdAndRoleIdAndProjectId(Long userId, Long tenantId, String roleId, Long projectId);
}