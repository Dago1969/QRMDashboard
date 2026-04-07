package com.qtm.dashboard.user.mapper;

import com.qtm.commonlib.dto.UserDto;
import com.qtm.dashboard.user.entity.UserEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper utente entity/dto.
 */
@Component
public class UserMapper {


    public UserDto toDto(UserEntity entity) {
        UserDto dto = new UserDto();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setEnabled(entity.isEnabled());
        dto.setRoleId(entity.getRole() != null ? entity.getRole().getId() : null);
        dto.setStructureId(entity.getStructureId());
        dto.setEmail(entity.getEmail());
        // Mappa il campo passwordHash della entity nel campo password del DTO (solo per provisioning o uso tecnico)
        dto.setPassword(entity.getPasswordHash());
        dto.setTelefono(entity.getTelefono());
        dto.setCodiceFiscale(entity.getCodiceFiscale());
        dto.setDataFineValiditaPassword(entity.getDataFineValiditaPassword());
        dto.setCanaleOtp(entity.getCanaleOtp());
        return dto;
    }

    public UserEntity toEntity(UserDto dto) {
        UserEntity entity = new UserEntity();
        entity.setId(dto.getId());
        entity.setUsername(dto.getUsername());
        entity.setEnabled(dto.isEnabled());
        entity.setStructureId(dto.getStructureId());
        entity.setEmail(dto.getEmail());
        // Mappa il campo password del DTO nel campo passwordHash della entity (solo per provisioning o uso tecnico)
        entity.setPasswordHash(dto.getPassword());
        entity.setTelefono(dto.getTelefono());
        entity.setCodiceFiscale(dto.getCodiceFiscale());
        entity.setDataFineValiditaPassword(dto.getDataFineValiditaPassword());
        entity.setCanaleOtp(dto.getCanaleOtp());
        return entity;
    }
}
