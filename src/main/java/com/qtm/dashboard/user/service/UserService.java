package com.qtm.dashboard.user.service;

import com.qtm.dashboard.user.dto.RegisterRequest;
import com.qtm.commonlib.dto.UserDto;
import com.qtm.dashboard.user.entity.RoleEntity;
import com.qtm.dashboard.user.entity.UserEntity;
import com.qtm.dashboard.user.mapper.UserMapper;
import com.qtm.dashboard.user.repository.RoleRepository;
import com.qtm.dashboard.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Service orchestratore CRUD utenti centralizzati.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    @Transactional(readOnly = true)
    public UserEntity findEntityByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Utente non trovato"));
    }

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final UserProvisioningService userProvisioningService;

    @Transactional
    public UserDto create(UserDto userDto) {
        return userProvisioningService.provisionUser(userDto);
    }

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(userMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<UserDto> search(String username, String email, String roleId, Long structureId, Boolean enabled) {
        return userRepository.findAll().stream()
            .filter(user -> containsIgnoreCase(user.getUsername(), username))
            .filter(user -> containsIgnoreCase(user.getEmail(), email))
            .filter(user -> containsIgnoreCase(user.getRole() != null ? user.getRole().getId() : null, roleId))
            .filter(user -> structureId == null || structureId.equals(user.getStructureId()))
            .filter(user -> enabled == null || user.isEnabled() == enabled)
            .map(userMapper::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        return userMapper.toDto(findEntityById(id));
    }

    @Transactional
    public UserDto update(Long id, UserDto userDto) {
        UserEntity current = findEntityById(id);
        validateUsernameUniqueness(userDto.getUsername(), id);
        current.setUsername(userDto.getUsername());
        current.setEnabled(userDto.isEnabled());
        current.setRole(findRoleById(userDto.getRoleId()));
        current.setStructureId(userDto.getStructureId());
        return userMapper.toDto(userRepository.save(current));
    }

    @Transactional
    public void delete(Long id) {
        userRepository.delete(findEntityById(id));
    }

    @Transactional
    public UserDto registerUser(RegisterRequest request) {
        UserDto userDto = new UserDto();
        userDto.setUsername(request.getUsername());
        userDto.setEnabled(true);
        userDto.setRoleId(resolveRegistrationRoleId(request));
        return create(userDto);
    }

    private UserEntity findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Utente non trovato"));
    }

    private boolean containsIgnoreCase(String source, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }

        if (source == null) {
            return false;
        }

        return source.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT));
    }

    private RoleEntity findRoleById(String roleId) {
        if (roleId == null || roleId.isBlank()) {
            return null;
        }

        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Ruolo non trovato"));
    }

    private void validateUsernameUniqueness(String username, Long currentId) {
        if (username == null || username.isBlank()) {
            return;
        }

        userRepository.findByUsernameIgnoreCase(username.trim())
                .ifPresent(existing -> {
                    if (currentId == null || !existing.getId().equals(currentId)) {
                        throw new ResponseStatusException(CONFLICT, "Username gia presente: " + username.trim());
                    }
                });
    }

    private String resolveRegistrationRoleId(RegisterRequest request) {
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            return "USER";
        }

        return request.getRoles().stream()
                .filter(role -> role != null && !role.isBlank())
                .map(role -> role.trim().toUpperCase(Locale.ROOT))
                .findFirst()
                .orElse("USER");
    }
}
