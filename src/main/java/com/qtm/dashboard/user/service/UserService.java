package com.qtm.dashboard.user.service;

import com.qtm.dashboard.user.dto.RegisterRequest;
import com.qtm.dashboard.user.dto.UserDto;
import com.qtm.dashboard.user.entity.RoleEntity;
import com.qtm.dashboard.user.entity.UserEntity;
import com.qtm.dashboard.user.mapper.UserMapper;
import com.qtm.dashboard.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.springframework.http.HttpStatus.CONFLICT;

/**
 * Service per l'orchestrazione repository/mapper nella gestione utenti.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository,
                       RoleService roleService,
                       PasswordEncoder passwordEncoder,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @SuppressWarnings("null")
    @Transactional
    public UserDto registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(CONFLICT, "Username già presente");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(CONFLICT, "Email già presente");
        }

        Set<RoleEntity> roles = roleService.findOrCreateRoles(request.getRoles());
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        UserEntity toSave = userMapper.toEntity(request.getUsername(), request.getEmail(), encodedPassword, roles);
        UserEntity saved = Objects.requireNonNull(userRepository.save(toSave));
        return userMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<UserDto> listUsers() {
        return userRepository.findAll().stream().map(userMapper::toDto).toList();
    }
}
