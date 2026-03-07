package com.qtm.dashboard.tenant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Mappa il client selezionato al relativo endpoint della TENANTS-APP.
 */
@Entity
@Table(
        name = "tenant_app_pointer",
        uniqueConstraints = @UniqueConstraint(name = "uk_tenant_app_pointer_client_code", columnNames = "client_code")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantAppPointerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_code", nullable = false, length = 100)
    private String clientCode;

    @Column(name = "client_name", nullable = false, length = 255)
    private String clientName;

    @Column(name = "tenant_app_url", nullable = false, length = 512)
    private String tenantAppUrl;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;
}
