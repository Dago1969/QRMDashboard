package com.qtm.dashboard.user.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Chiave composta dell'associazione utente-tenant-ruolo per il profilo applicativo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserRoleProfileId implements Serializable {

    private Long userId;
    private Long tenantId;
    private String roleId;
    private Long profileId;
}