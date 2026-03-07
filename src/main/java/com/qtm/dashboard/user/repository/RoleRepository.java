package com.qtm.dashboard.user.repository;

import com.qtm.dashboard.user.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository JPA per la gestione dei ruoli applicativi.
 */
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);
}
