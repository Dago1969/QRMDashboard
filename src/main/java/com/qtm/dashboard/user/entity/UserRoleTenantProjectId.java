package com.qtm.dashboard.user.entity;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Composite key per UserRoleTenantProjectEntity.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class UserRoleTenantProjectId implements Serializable {
    private Long userId;
    private Long tenantId;
    private String roleId;
    private String projectId;
}
