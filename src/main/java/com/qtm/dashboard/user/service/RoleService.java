package com.qtm.dashboard.user.service;

import com.qtm.dashboard.user.dto.RoleDeleteCheckDto;
import com.qtm.dashboard.user.dto.RoleDeleteLinkedUserDto;
import com.qtm.commonlib.dto.RoleDto;
import com.qtm.dashboard.user.entity.RoleEntity;
import com.qtm.dashboard.user.entity.UserEntity;
import com.qtm.dashboard.user.mapper.RoleMapper;
import com.qtm.dashboard.user.repository.RoleRepository;
import com.qtm.dashboard.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Service orchestratore CRUD ruoli centralizzati.
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final UserRepository userRepository;

    @Transactional
    public RoleDto create(RoleDto roleDto) {
        return roleMapper.toDto(roleRepository.save(roleMapper.toEntity(roleDto)));
    }

    @Transactional(readOnly = true)
    public List<RoleDto> findAll() {
        return roleRepository.findAll().stream().map(roleMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public RoleDto findById(String id) {
        return roleMapper.toDto(findEntityById(id));
    }

    @Transactional
    public RoleDto update(String id, RoleDto roleDto) {
        RoleEntity current = findEntityById(id);
        current.setName(roleDto.getName() == null || roleDto.getName().isBlank() ? roleDto.getDescription() : roleDto.getName());
        current.setDescription(roleDto.getDescription());
        return roleMapper.toDto(roleRepository.save(current));
    }

    @Transactional(readOnly = true)
    public RoleDeleteCheckDto getDeleteCheck(String id) {
        RoleEntity role = findEntityById(id);
        List<RoleDeleteLinkedUserDto> linkedUsers = userRepository.findAllByRoleId(id).stream()
                .map(user -> new RoleDeleteLinkedUserDto(user.getId(), user.getUsername()))
                .toList();
        List<RoleDto> replacementRoles = roleRepository.findAll().stream()
                .filter(currentRole -> !currentRole.getId().equals(role.getId()))
                .map(roleMapper::toDto)
                .toList();
        return new RoleDeleteCheckDto(role.getId(), linkedUsers, replacementRoles);
    }

    @Transactional
    public void delete(String id, String replacementRoleId) {
        RoleEntity role = findEntityById(id);
        List<UserEntity> linkedUsers = userRepository.findAllByRoleId(id);
        if (!linkedUsers.isEmpty()) {
            if (replacementRoleId == null || replacementRoleId.isBlank()) {
                throw new ResponseStatusException(BAD_REQUEST, "Esistono utenti collegati al ruolo da cancellare");
            }
            if (id.equalsIgnoreCase(replacementRoleId)) {
                throw new ResponseStatusException(BAD_REQUEST, "Il nuovo ruolo deve essere diverso dal ruolo da cancellare");
            }
            RoleEntity replacementRole = findEntityById(replacementRoleId);
            linkedUsers.forEach(user -> user.setRole(replacementRole));
            userRepository.saveAll(linkedUsers);
        }

        roleRepository.delete(role);
    }

    private RoleEntity findEntityById(String id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Ruolo non trovato"));
    }
}
