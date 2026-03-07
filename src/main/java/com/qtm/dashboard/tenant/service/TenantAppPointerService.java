package com.qtm.dashboard.tenant.service;

import com.qtm.dashboard.tenant.dto.TenantAppPointerDto;
import com.qtm.dashboard.tenant.dto.TenantAppPointerUpsertRequestDto;
import com.qtm.dashboard.tenant.dto.TenantResolutionDto;
import com.qtm.dashboard.tenant.entity.TenantAppPointerEntity;
import com.qtm.dashboard.tenant.mapper.TenantAppPointerMapper;
import com.qtm.dashboard.tenant.repository.TenantAppPointerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Service di orchestrazione CRUD e risoluzione veloce per puntamenti tenant app.
 */
@Service
@RequiredArgsConstructor
public class TenantAppPointerService {

    private final TenantAppPointerRepository tenantAppPointerRepository;
    private final TenantAppPointerMapper tenantAppPointerMapper;

    @Transactional(readOnly = true)
    public List<TenantAppPointerDto> listAll() {
        return tenantAppPointerRepository.findAll().stream()
                .map(tenantAppPointerMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TenantAppPointerDto getById(Long id) {
        TenantAppPointerEntity entity = findByIdOrThrow(id);
        return tenantAppPointerMapper.toDto(entity);
    }

    @Transactional
    public TenantAppPointerDto create(TenantAppPointerUpsertRequestDto request) {
        validateClientCodeUniqueness(request.getClientCode(), null);
        TenantAppPointerEntity created = tenantAppPointerMapper.toEntity(request);
        TenantAppPointerEntity saved = tenantAppPointerRepository.save(created);
        return tenantAppPointerMapper.toDto(saved);
    }

    @Transactional
    public TenantAppPointerDto update(Long id, TenantAppPointerUpsertRequestDto request) {
        TenantAppPointerEntity existing = findByIdOrThrow(id);
        validateClientCodeUniqueness(request.getClientCode(), id);
        tenantAppPointerMapper.updateEntity(existing, request);
        TenantAppPointerEntity saved = tenantAppPointerRepository.save(existing);
        return tenantAppPointerMapper.toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        TenantAppPointerEntity entity = findByIdOrThrow(id);
        tenantAppPointerRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public TenantResolutionDto resolveActiveTenantUrl(String clientCode) {
        TenantAppPointerEntity pointer = tenantAppPointerRepository.findByClientCodeAndEnabledTrue(clientCode)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND,
                        "Nessun puntamento attivo trovato per client_code: " + clientCode
                ));

        return TenantResolutionDto.builder()
                .clientCode(pointer.getClientCode())
                .tenantAppUrl(pointer.getTenantAppUrl())
                .build();
    }

    @Transactional(readOnly = true)
    public TenantResolutionDto resolveActiveTenantUrlForJwt(String clientCode, Jwt jwt) {
        if (!isClientInResourceAccess(jwt, clientCode)) {
            throw new ResponseStatusException(
                    FORBIDDEN,
                    "Il client_code non e presente in resource_access: " + clientCode
            );
        }
        return resolveActiveTenantUrl(clientCode);
    }

    private TenantAppPointerEntity findByIdOrThrow(Long id) {
        return tenantAppPointerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Puntamento tenant non trovato: " + id));
    }

    private void validateClientCodeUniqueness(String clientCode, Long currentId) {
        tenantAppPointerRepository.findByClientCode(clientCode)
                .ifPresent(existing -> {
                    if (currentId == null || !existing.getId().equals(currentId)) {
                        throw new ResponseStatusException(CONFLICT, "client_code gia presente: " + clientCode);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private boolean isClientInResourceAccess(Jwt jwt, String clientCode) {
        Object resourceAccessObj = jwt.getClaim("resource_access");
        if (!(resourceAccessObj instanceof Map<?, ?> resourceAccess)) {
            return false;
        }
        return ((Map<String, Object>) resourceAccess).containsKey(clientCode);
    }
}
