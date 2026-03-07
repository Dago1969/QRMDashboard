package com.qtm.dashboard.user.mapper;

import com.qtm.dashboard.user.dto.UserDto;
import com.qtm.dashboard.user.entity.RoleEntity;
import com.qtm.dashboard.user.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper dedicato alla conversione UserEntity -> UserDto.
 */
@Component
public class UserMapper {

    private final RoleMapper roleMapper;

    public UserMapper(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    public UserDto toDto(UserEntity entity) {
        UserDto dto = new UserDto();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setEnabled(entity.isEnabled());
        dto.setRoles(entity.getRoles().stream().map(roleMapper::toDto).toList());
        return dto;
    }

    public UserEntity toEntity(String username, String email, String encodedPassword, Iterable<RoleEntity> roles) {
        UserEntity entity = new UserEntity();
        entity.setUsername(username);
        entity.setEmail(email);
        entity.setPasswordHash(encodedPassword);
        entity.setEnabled(true);
        entity.setRoles(
                java.util.stream.StreamSupport.stream(roles.spliterator(), false)
                        .collect(Collectors.toSet())
        );
        return entity;
    }
}
