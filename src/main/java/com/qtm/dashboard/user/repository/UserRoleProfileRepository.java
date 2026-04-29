package com.qtm.dashboard.user.repository;

import com.qtm.dashboard.user.entity.UserRoleProfileEntity;
import com.qtm.dashboard.user.entity.UserRoleProfileId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository per la tabella user_role_profile.
 */
public interface UserRoleProfileRepository extends JpaRepository<UserRoleProfileEntity, UserRoleProfileId> {
}