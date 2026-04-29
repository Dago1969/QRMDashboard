package com.qtm.dashboard.user.service;

import com.qtm.dashboard.mail.repository.MailTemplateRepository;
import com.qtm.dashboard.mail.service.MailService;
import com.qtm.dashboard.user.entity.UserEntity;
import com.qtm.dashboard.user.mapper.UserMapper;
import com.qtm.dashboard.user.repository.RoleRepository;
import com.qtm.dashboard.user.repository.UserRepository;
import com.qtm.dashboard.user.repository.UserRoleProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;

/**
 * Verifica che la cancellazione utente pulisca le relazioni locali e sincronizzi Keycloak.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleProjectRepository userRoleProjectRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserProvisioningService userProvisioningService;

    @Mock
    private MailTemplateRepository mailTemplateRepository;

    @Mock
    private MailService mailService;

    @InjectMocks
    private UserService userService;

    @Test
    void deleteShouldRemoveRelationsAndDeleteUserFromKeycloak() {
        UserEntity user = new UserEntity();
        user.setId(42L);
        user.setUsername("utente.test");

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));

        userService.delete(42L);

        verify(userProvisioningService).deleteUserFromKeycloak("utente.test");
        verify(userRoleProjectRepository).deleteByUserId(42L);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteShouldStopBeforeLocalDeletionWhenKeycloakDeletionFails() {
        UserEntity user = new UserEntity();
        user.setId(42L);
        user.setUsername("utente.test");

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        ResponseStatusException keycloakFailure = new ResponseStatusException(BAD_GATEWAY, "Keycloak non raggiungibile");
        org.mockito.Mockito.doThrow(keycloakFailure)
                .when(userProvisioningService)
                .deleteUserFromKeycloak("utente.test");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.delete(42L));

        assertEquals(BAD_GATEWAY, exception.getStatusCode());
        verify(userRoleProjectRepository, never()).deleteByUserId(42L);
        verify(userRepository, never()).delete(user);
    }
}