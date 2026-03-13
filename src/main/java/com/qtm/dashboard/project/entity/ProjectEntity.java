package com.qtm.dashboard.project.entity;

import com.qtm.dashboard.tenant.entity.TenantAppPointerEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity progetto centralizzata su QTMDashboard con riferimento al tenant cliente.
 */
@Entity
@Table(
        name = "projects",
        uniqueConstraints = @UniqueConstraint(name = "uk_projects_code_tenant", columnNames = {"code", "tenant_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 255)
    private String code;

    /**
     * Descrizione del progetto.
     */
    @Column(name = "descrizione", length = 1024)
    private String descrizione;

    /**
     * Data di inizio progetto.
     */
    @Column(name = "data_inizio")
    private java.time.LocalDate dataInizio;

    /**
     * Data di fine progetto.
     */
    @Column(name = "data_fine")
    private java.time.LocalDate dataFine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantAppPointerEntity tenant;
}