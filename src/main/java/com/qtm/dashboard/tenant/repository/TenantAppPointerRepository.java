package com.qtm.dashboard.tenant.repository;

import com.qtm.dashboard.tenant.entity.TenantAppPointerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository per gestire i puntamenti verso le varie TENANTS-APP per client.
 */
public interface TenantAppPointerRepository extends JpaRepository<TenantAppPointerEntity, Long> {

    Optional<TenantAppPointerEntity> findByClientCode(String clientCode);

    Optional<TenantAppPointerEntity> findByClientCodeAndEnabledTrue(String clientCode);

    boolean existsByClientCode(String clientCode);
}
