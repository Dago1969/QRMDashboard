package com.qtm.dashboard.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity centralizzata per le associazioni utente-tenant-ruolo-profilo.
 */
@Entity
@Table(name = "user_role_project")
@Getter
@Setter
@NoArgsConstructor
@IdClass(UserRoleProjectId.class)
public class UserRoleProjectEntity {
 
    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Id
    @Column(name = "role_id", nullable = false, length = 100)
    private String roleId;

    @Id
    @Column(name = "project_id", nullable = false, length = 100)
    private String projectId;
}