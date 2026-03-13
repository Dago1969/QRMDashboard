package com.qtm.dashboard.user.entity;

import com.qtm.dashboard.project.entity.ProjectEntity;
import com.qtm.dashboard.tenant.entity.TenantAppPointerEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Relazione tra User, Tenant e Progetto.
 * project può essere nullo: utente globale sul tenant.
 */
@Entity
@Table(name = "user_tenant_project")
@Getter
@Setter
@NoArgsConstructor
public class UserTenantProjectRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantAppPointerEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectEntity project; // può essere nullo

    @Column(name = "superuser", nullable = false)
    private boolean superuser = false;

    /**
     * Email dell'utente (denormalizzato, opzionale per query rapide).
     */
    @Column(name = "email")
    private String email;
}
