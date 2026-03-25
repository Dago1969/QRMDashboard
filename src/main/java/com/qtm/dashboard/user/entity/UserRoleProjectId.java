package com.qtm.dashboard.user.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Chiave composta per user_role_project.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class UserRoleProjectId implements Serializable {
    private Long userId;
    private Long tenantId;
    private String roleId;
    private String projectId;
}