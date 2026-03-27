package com.qtm.dashboard.user.service;

import com.qtm.commonlib.dto.UserRoleProjectDto;
import com.qtm.dashboard.tenant.repository.TenantAppPointerRepository;
import com.qtm.dashboard.tenant.entity.TenantAppPointerEntity;
import com.qtm.dashboard.user.entity.RoleEntity;
import com.qtm.dashboard.user.entity.UserEntity;
import com.qtm.dashboard.user.entity.UserRoleProjectEntity;
import com.qtm.dashboard.user.repository.RoleRepository;
import com.qtm.dashboard.user.repository.UserRepository;
import com.qtm.dashboard.user.repository.UserRoleProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import com.qtm.commonlib.dto.UserDto;

/**
 * Service centralizzato per le associazioni utente-tenant-ruolo-progetto.
 */
@Service
@RequiredArgsConstructor

public class UserRoleProjectService {

    private final UserRoleProjectRepository repository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantAppPointerRepository tenantAppPointerRepository;
    private final UserProvisioningService userProvisioningService;


    @Transactional(readOnly = true)
    public List<UserRoleProjectDto> findByUserAndTenant(Long userId, Long tenantId) {
        return repository.findByUserIdAndTenantIdOrderByRoleIdAscProjectIdAsc(userId, tenantId).stream()
                .map(this::toDto)
                .toList();
    }


    /**
     * Salva una nuova associazione user+tenant+ruolo+progetto e aggiorna Keycloak se necessario.
     */
    @Transactional
    public UserRoleProjectDto save(UserRoleProjectDto dto) {
        findUser(dto.getUserId());
        findRole(dto.getRoleId());
        findTenant(dto.getTenantId());

        if (dto.getProjectId() == null) {
            throw new ResponseStatusException(NOT_FOUND, "Progetto non valorizzato");
        }

        // Verifica se è la prima associazione per quell'utente/tenant/ruolo
        long count = repository.findByUserId(dto.getUserId()).stream()
            .filter(e -> e.getTenantId().equals(dto.getTenantId()) && e.getRoleId().equals(dto.getRoleId()))
            .count();

        UserRoleProjectEntity entity = new UserRoleProjectEntity();
        entity.setUserId(dto.getUserId());
        entity.setTenantId(dto.getTenantId());
        entity.setRoleId(dto.getRoleId());
        entity.setProjectId(dto.getProjectId());
        UserRoleProjectDto saved = toDto(repository.save(entity));

        // Se era la prima associazione, aggiungi ruolo in Keycloak
        if (count == 0) {
            UserEntity user = findUser(dto.getUserId());
            TenantAppPointerEntity tenant = tenantAppPointerRepository.findById(dto.getTenantId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tenant non trovato"));
            UserDto userDto = new UserDto();
            userDto.setUsername(user.getUsername());
            userDto.setRoleId(dto.getRoleId());
            userDto.setClientId(tenant.getClientCode());
            userDto.setEnabled(user.isEnabled());
            userDto.setEmail(user.getEmail());
            userProvisioningService.provisionUser(userDto);
        }
        return saved;
    }


    /**
     * Cancella una associazione user+tenant+ruolo+progetto e aggiorna Keycloak se necessario.
     */
    @Transactional
    public void delete(Long userId, Long tenantId, String roleId, Long projectId) {
        repository.deleteByUserIdAndTenantIdAndRoleIdAndProjectId(userId, tenantId, roleId, projectId);

        // Dopo la cancellazione, verifica se esistono ancora altre associazioni per quell'utente/tenant/ruolo
        long count = repository.findByUserId(userId).stream()
            .filter(e -> e.getTenantId().equals(tenantId) && e.getRoleId().equals(roleId))
            .count();
        if (count == 0) {
            // Se non ci sono più associazioni, rimuovi il ruolo dal client in Keycloak
            UserEntity user = findUser(userId);
            TenantAppPointerEntity tenant = tenantAppPointerRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tenant non trovato"));
            UserDto userDto = new UserDto();
            userDto.setUsername(user.getUsername());
            userDto.setRoleId(roleId);
            userDto.setClientId(tenant.getClientCode());
            userDto.setEnabled(user.isEnabled());
            userDto.setEmail(user.getEmail());
            // Rimuovi il ruolo dal client in Keycloak
            userProvisioningService.removeUserRoleFromClient(userDto);
        }
    }

    private UserRoleProjectDto toDto(UserRoleProjectEntity entity) {
        UserRoleProjectDto dto = new UserRoleProjectDto();
        dto.setUserId(entity.getUserId());
        dto.setTenantId(entity.getTenantId());
        dto.setRoleId(entity.getRoleId());
        dto.setProjectId(entity.getProjectId());
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

    private void findTenant(Long tenantId) {
        tenantAppPointerRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tenant non trovato"));
    }
}