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

import com.qtm.dashboard.user.entity.UserTenantRoleRelation;

/**
 * Service di compatibilita per le API user-tenant-role basato sul ruolo singolo attualmente associato all'utente.
 */

@Service
@RequiredArgsConstructor
public class UserTenantRoleRelationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final com.qtm.dashboard.user.repository.UserTenantRoleRelationRepository userTenantRoleRelationRepository;


    @Transactional(readOnly = true)
    public List<UserTenantRoleRelationDto> findByUserAndTenant(Long userId, Long tenantId) {
        List<UserTenantRoleRelation> relations = userTenantRoleRelationRepository.findByUserIdAndTenantId(userId, tenantId);
        return relations.stream().map(this::toDto).toList();
    }


    @Transactional
    public UserTenantRoleRelationDto save(UserTenantRoleRelationDto dto) {
        // Verifica esistenza user e ruolo
        findUser(dto.getUserId());
        findRole(dto.getRoleId());
        UserTenantRoleRelation rel = new UserTenantRoleRelation();
        rel.setUserId(dto.getUserId());
        rel.setTenantId(dto.getTenantId());
        rel.setRoleId(dto.getRoleId());
        UserTenantRoleRelation saved = userTenantRoleRelationRepository.save(rel);
        return toDto(saved);
    }



    @Transactional
    public UserTenantRoleRelationDto saveSIngle(UserTenantRoleRelationDto dto) {
        return save(dto);
    }


    @Transactional
    public void delete(Long relationId) {
        // Per compatibilità, relationId = userId
        // Elimina tutte le relazioni per quell'user (su tutti i tenant e ruoli)
        List<UserTenantRoleRelation> rels = userTenantRoleRelationRepository.findByUserIdAndTenantId(relationId, null);
        userTenantRoleRelationRepository.deleteAll(rels);
    }

    @Transactional
    public void deleteByUserTenantRole(Long userId, Long tenantId, String roleId) {
        userTenantRoleRelationRepository.deleteByUserIdAndTenantIdAndRoleId(userId, tenantId, roleId);
    }


    private UserTenantRoleRelationDto toDto(UserTenantRoleRelation rel) {
        UserTenantRoleRelationDto dto = new UserTenantRoleRelationDto();
        dto.setUserId(rel.getUserId());
        dto.setTenantId(rel.getTenantId());
        dto.setRoleId(rel.getRoleId());
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