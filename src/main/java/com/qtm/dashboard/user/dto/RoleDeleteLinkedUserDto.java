package com.qtm.dashboard.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO utente collegato al ruolo in fase di pre-cancellazione.
 */
@Getter
@AllArgsConstructor
public class RoleDeleteLinkedUserDto {

    private Long id;
    private String username;
}
