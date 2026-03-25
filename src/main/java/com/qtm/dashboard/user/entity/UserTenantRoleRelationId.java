package com.qtm.dashboard.user.entity;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Chiave composta per UserTenantRoleRelation.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class UserTenantRoleRelationId implements Serializable {
    private Long userId;
    private Long tenantId;
    private String roleId;
}
