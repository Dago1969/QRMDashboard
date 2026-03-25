package com.qtm.dashboard.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import com.qtm.commonlib.dto.RoleDto;

/**
 * DTO di pre-cancellazione ruolo con utenti collegati e ruoli alternativi.
 */
@Getter
@AllArgsConstructor
public class RoleDeleteCheckDto {

    private String roleId;
    private List<RoleDeleteLinkedUserDto> linkedUsers;
    private List<RoleDto> replacementRoles;
}
