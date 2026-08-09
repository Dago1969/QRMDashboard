package com.qtm.dashboard.auth.service;

import com.qtm.dashboard.config.KeycloakProperties;
import com.qtm.dashboard.user.entity.UserEntity;
import com.qtm.dashboard.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.env.Environment;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Verifica che il login provi sempre l'identificativo canonico dell'utente prima di fallire.
 */
@ExtendWith(MockitoExtension.class)
class KeycloakAuthServiceTest {

    @Mock
    private KeycloakProperties keycloakProperties;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Environment environment;

    private KeycloakAuthService keycloakAuthService;

    @BeforeEach
    void setUp() {
        keycloakAuthService = new KeycloakAuthService(keycloakProperties, userRepository, environment);
    }

    @Test
    void resolveLoginIdentifiersShouldRetryWithCanonicalUsernameResolvedFromEmail() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("pinco");

        when(userRepository.findByUsernameIgnoreCase("pinco@almaviva.it")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("pinco@almaviva.it")).thenReturn(Optional.of(userEntity));

        List<String> result = keycloakAuthService.resolveLoginIdentifiers("  pinco@almaviva.it  ");

        assertEquals(List.of("pinco@almaviva.it", "pinco"), result);
    }

    @Test
    void resolveLoginIdentifiersShouldAppendCanonicalUsernameWhenOnlyCaseDiffers() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("pinco");

        when(userRepository.findByUsernameIgnoreCase("Pinco")).thenReturn(Optional.of(userEntity));
        when(userRepository.findByEmailIgnoreCase("Pinco")).thenReturn(Optional.empty());

        List<String> result = keycloakAuthService.resolveLoginIdentifiers("  Pinco ");

        assertEquals(List.of("Pinco", "pinco"), result);
    }
}