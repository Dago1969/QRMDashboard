
package com.qtm.dashboard.user.service;

import com.qtm.commonlib.dto.UserDto;
import com.qtm.dashboard.mail.entity.MailTemplateEntity;
import com.qtm.dashboard.mail.repository.MailTemplateRepository;
import com.qtm.dashboard.mail.service.MailService;
import com.qtm.dashboard.user.dto.RegisterRequest;
import com.qtm.dashboard.user.entity.RoleEntity;
import com.qtm.dashboard.user.entity.UserEntity;
import com.qtm.dashboard.user.mapper.UserMapper;
import com.qtm.dashboard.user.repository.RoleRepository;
import com.qtm.dashboard.user.repository.UserRoleProfileRepository;
import com.qtm.dashboard.user.repository.UserRoleProjectRepository;
import com.qtm.dashboard.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Service orchestratore CRUD utenti centralizzati.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private static final int PASSWORD_RESET_TOKEN_VALIDITY_HOURS = 2;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleProfileRepository userRoleProfileRepository;
    private final UserRoleProjectRepository userRoleProjectRepository;
    private final UserMapper userMapper;
    private final UserProvisioningService userProvisioningService;
    private final MailTemplateRepository mailTemplateRepository;
    private final MailService mailService;

    @Value("${qtm.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    @Transactional(readOnly = true)
    public UserEntity findEntityByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Utente non trovato"));
    }

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
        UserEntity user = findEntityById(id);

        // Mantiene allineati DB locale e Keycloak durante la rimozione utente.
        userProvisioningService.deleteUserFromKeycloak(user.getUsername());
        // Elimina tutte le relazioni user_role_profile e user_role_project prima di eliminare l'utente
        userRoleProfileRepository.deleteByUserId(id);
        userRoleProjectRepository.deleteByUserId(id);
        userRepository.delete(user);
    }

    @Transactional
    public UserDto registerUser(RegisterRequest request) {
        UserDto userDto = new UserDto();
        userDto.setUsername(request.getUsername());
        userDto.setEmail(request.getEmail());
        userDto.setEnabled(true);
        userDto.setRoleId(resolveRegistrationRoleId(request));
        return create(userDto);
    }

    /**
     * Gestisce la richiesta di recupero password senza esporre se l'email esiste.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePasswordRecover(String email) {
        userRepository.findByEmailIgnoreCase(email.trim())
                .filter(UserEntity::isEnabled)
                .ifPresentOrElse(user -> {
                    String token = UUID.randomUUID().toString();
                    user.setPasswordResetToken(token);
                    user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(PASSWORD_RESET_TOKEN_VALIDITY_HOURS));
                    userRepository.save(user);

                    String resetUrl = frontendBaseUrl + "/reset-password?token=" + token;
                        log.info("URL reset password generato per email={}: {}", user.getEmail(), resetUrl);
                    String language = "it";
                    MailTemplateEntity template = mailTemplateRepository
                            .findByCodeAndLanguageAndEnabledTrue("PASSWORD_RECOVER", language)
                            .or(() -> mailTemplateRepository.findByCodeAndEnabledTrue("PASSWORD_RECOVER"))
                            .orElse(null);

                    if (template == null) {
                        log.warn("Nessun template PASSWORD_RECOVER abilitato trovato");
                        return;
                    }

                    String body = template.getBody()
                            .replace("${firstName}", user.getUsername())
                            .replace("${resetUrl}", resetUrl);

                    boolean sent = mailService.send(user.getEmail(), template.getSubject(), body);
                    if (sent) {
                        log.info("Mail di recupero password inviata a {}", user.getEmail());
                    }
                }, () -> log.info("Richiesta reset password per email non presente: {}", email));
    }

    @Transactional(readOnly = true)
    public UserEntity findByValidPasswordResetToken(String token) {
        LocalDateTime now = LocalDateTime.now();
        return userRepository.findByPasswordResetToken(token)
                .filter(user -> user.getPasswordResetTokenExpiry() != null)
                .filter(user -> user.getPasswordResetTokenExpiry().isAfter(now))
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Token reset password non valido o scaduto"));
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
