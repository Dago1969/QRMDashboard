package com.qtm.dashboard.user.dto;

import lombok.Data;

/**
 * DTO della dashboard che rappresenta l'accesso utente a un progetto derivato da user_role_project.
 */
@Data
public class DashboardUserProjectDto {
    private Long userId;
    private Long tenantId;
    private String tenantCode;
    private String tenantName;
    private Long projectId;
    private String projectCode;
    private String projectDescription;
    private String roleId;
    private boolean superuser;
}