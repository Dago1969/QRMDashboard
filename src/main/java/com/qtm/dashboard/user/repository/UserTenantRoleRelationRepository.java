package com.qtm.dashboard.user.repository;

import com.qtm.dashboard.user.entity.UserTenantRoleRelation;
import com.qtm.dashboard.user.entity.UserTenantRoleRelationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository per la tabella user_tenant_role.
 */
@Repository
public interface UserTenantRoleRelationRepository extends JpaRepository<UserTenantRoleRelation, UserTenantRoleRelationId> {
    List<UserTenantRoleRelation> findByUserIdAndTenantId(Long userId, Long tenantId);
    void deleteByUserIdAndTenantIdAndRoleId(Long userId, Long tenantId, String roleId);
}
