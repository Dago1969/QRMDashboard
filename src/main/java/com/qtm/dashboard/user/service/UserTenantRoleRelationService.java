package com.qtm.dashboard.user.service;

import com.qtm.commonlib.dto.UserTenantRoleRelationDto;
import com.qtm.dashboard.user.entity.RoleEntity;
import com.qtm.dashboard.user.entity.UserEntity;
import com.qtm.dashboard.user.repository.RoleRepository;
import com.qtm.dashboard.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Service di compatibilita per le API user-tenant-role basato sul ruolo singolo attualmente associato all'utente.
 */
@Service
@RequiredArgsConstructor
public class UserTenantRoleRelationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<UserTenantRoleRelationDto> findByUserAndTenant(Long userId, Long tenantId) {
        UserEntity user = findUser(userId);
        if (user.getRole() == null) {
            return List.of();
        }

        return List.of(toDto(user, tenantId));
    }

    @Transactional
    public UserTenantRoleRelationDto save(UserTenantRoleRelationDto dto) {
        UserEntity user = findUser(dto.getUserId());
        RoleEntity role = findRole(dto.getRoleId());
        user.setRole(role);
        UserEntity saved = userRepository.save(user);
        return toDto(saved, dto.getTenantId());
    }

    @Transactional
    public void delete(Long relationId) {
        UserEntity user = findUser(relationId);
        user.setRole(null);
        userRepository.save(user);
    }

    private UserTenantRoleRelationDto toDto(UserEntity user, Long tenantId) {
        UserTenantRoleRelationDto dto = new UserTenantRoleRelationDto();
        dto.setId(user.getId());
        dto.setUserId(user.getId());
        dto.setTenantId(tenantId);
        dto.setRoleId(user.getRole().getId());
        return dto;
    }

    private UserEntity findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Utente non trovato"));
    }

    private RoleEntity findRole(String roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Ruolo non trovato"));
    }
}