package com.qtm.dashboard.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity per la tabella di relazione User-Tenant-Role.
 */
@Entity
@Table(name = "user_tenant_role")
@Getter
@Setter
@NoArgsConstructor
@IdClass(UserTenantRoleRelationId.class)
public class UserTenantRoleRelation {
    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Id
    @Column(name = "role_id", nullable = false)
    private String roleId;
}
