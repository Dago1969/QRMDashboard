package com.qtm.dashboard.user.service;

import com.qtm.dashboard.auth.dto.LoginRequest;
import com.qtm.dashboard.auth.dto.LoginResponse;
import com.qtm.dashboard.auth.service.KeycloakAuthService;
import com.qtm.dashboard.config.KeycloakProperties;
import com.qtm.dashboard.mail.repository.MailTemplateRepository;
import com.qtm.dashboard.mail.service.MailService;
import com.qtm.dashboard.user.mapper.UserMapper;
import com.qtm.dashboard.user.repository.RoleRepository;
import com.qtm.dashboard.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Verifica che il provisioning validi subito le credenziali appena generate contro Keycloak.
 */
@ExtendWith(MockitoExtension.class)
class UserProvisioningServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private KeycloakProperties keycloakProperties;

    @Mock
    private KeycloakAuthService keycloakAuthService;

    @Mock
    private MailTemplateRepository mailTemplateRepository;

    @Mock
    private MailService mailService;

    @Captor
    private ArgumentCaptor<LoginRequest> loginRequestCaptor;

    private UserProvisioningService userProvisioningService;

    @BeforeEach
    void setUp() {
        userProvisioningService = new UserProvisioningService(
                userRepository,
                roleRepository,
                userMapper,
                keycloakProperties,
            keycloakAuthService,
            mailTemplateRepository,
            mailService);
    }

    @Test
    void verifyKeycloakLoginShouldSendGeneratedCredentialsToKeycloak() {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken("token");
        when(keycloakAuthService.login(org.mockito.ArgumentMatchers.any(LoginRequest.class))).thenReturn(loginResponse);

        userProvisioningService.verifyKeycloakLogin("utente.test", "Password123!");

        verify(keycloakAuthService).login(loginRequestCaptor.capture());
        assertEquals("utente.test", loginRequestCaptor.getValue().getUsername());
        assertEquals("Password123!", loginRequestCaptor.getValue().getPassword());
    }

    @Test
    void verifyKeycloakLoginShouldRaiseFunctionalErrorWhenKeycloakRejectsCredentials() {
        when(keycloakAuthService.login(org.mockito.ArgumentMatchers.any(LoginRequest.class)))
                .thenThrow(new ResponseStatusException(UNAUTHORIZED, "Credenziali non valide"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userProvisioningService.verifyKeycloakLogin("utente.test", "Password123!"));

        assertEquals(UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Accesso fallito: verifica le credenziali Keycloak.", exception.getReason());
    }

    @Test
    void resolveKeycloakProfileNamesShouldUseEmailTokensWhenAvailable() {
        assertEquals("Mario", userProvisioningService.resolveKeycloakFirstName("m.rossi", "mario.rossi@example.com"));
        assertEquals("Rossi", userProvisioningService.resolveKeycloakLastName("m.rossi", "mario.rossi@example.com"));
    }

    @Test
    void resolveKeycloakProfileNamesShouldFallbackWhenOnlyOneTokenIsAvailable() {
        assertEquals("Utente", userProvisioningService.resolveKeycloakFirstName("__", null));
        assertEquals("Qtm", userProvisioningService.resolveKeycloakLastName("pluto", null));
    }

    @Test
    void resolveClientRolesToAssignShouldNotFailWhenRequestedRoleIsAlreadyAssigned() throws Exception {
        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setClientId("app-cliente-A");

        RoleRepresentation assignedRole = new RoleRepresentation();
        assignedRole.setName("SUPER_ADMIN");

        Method method = UserProvisioningService.class.getDeclaredMethod(
                "resolveClientRolesToAssign",
                ClientRepresentation.class,
                List.class,
                List.class,
                List.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<RoleRepresentation> result = (List<RoleRepresentation>) method.invoke(
                userProvisioningService,
                clientRepresentation,
                List.of(assignedRole),
                List.of(),
                List.of("SUPER_ADMIN"));

        Assertions.assertTrue(result.isEmpty());
    }
}