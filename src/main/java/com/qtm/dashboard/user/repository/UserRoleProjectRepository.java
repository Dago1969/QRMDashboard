
package com.qtm.dashboard.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.qtm.dashboard.user.entity.UserRoleProjectEntity;
import com.qtm.dashboard.user.entity.UserRoleProjectId;

/**
 * Repository JPA per user_role_project.
 */
@Repository
public interface UserRoleProjectRepository extends JpaRepository<UserRoleProjectEntity, UserRoleProjectId> {

    List<UserRoleProjectEntity> findByTenantId(Long tenantId);
    List<UserRoleProjectEntity> findByProjectId(Long projectId);

    List<UserRoleProjectEntity> findByUserIdAndTenantIdOrderByRoleIdAscProjectIdAsc(Long userId, Long tenantId);

    void deleteByUserIdAndTenantIdAndRoleIdAndProjectId(Long userId, Long tenantId, String roleId, Long projectId);

	List<UserRoleProjectEntity> findByUserId(Long userId);

//	List<UserRoleProjectEntity> findByUserIdWithFetch(Long userId);
}