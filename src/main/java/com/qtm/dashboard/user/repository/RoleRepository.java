package com.qtm.dashboard.user.repository;

import com.qtm.dashboard.user.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository ruoli centralizzati.
 */
public interface RoleRepository extends JpaRepository<RoleEntity, String> {
}
