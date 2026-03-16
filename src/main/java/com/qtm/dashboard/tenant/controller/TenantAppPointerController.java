package com.qtm.dashboard.tenant.controller;

import com.qtm.commonlib.dto.TenantDto;
import com.qtm.dashboard.tenant.dto.TenantAppPointerUpsertRequestDto;
import com.qtm.dashboard.tenant.dto.TenantResolutionDto;
import com.qtm.dashboard.tenant.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST per gestione CRUD dei puntamenti client -> TENANTS-APP e risoluzione rapida URL.
 */
@RestController
@RequestMapping("/api/tenant-app-pointers")
@RequiredArgsConstructor
@Slf4j
public class TenantAppPointerController {

    private final TenantService tenantAppPointerService;

    @GetMapping
    public ResponseEntity<List<TenantDto>> listAll() {
        return ResponseEntity.ok(tenantAppPointerService.listAll());
    }

    /**
     * Ricerca puntamento per clientCode. Restituisce 200 con il DTO se presente, 404 se assente.
     */
    @GetMapping("/by-client/{clientCode}")
    public ResponseEntity<TenantDto> getByClientCode(@PathVariable String clientCode) {
        log.info("[TenantAppPointerController] Received lookup request for clientCode={}", clientCode);
        return tenantAppPointerService.findByClientCode(clientCode)
                .map(pointer -> {
                    log.info("[TenantAppPointerController] Returning tenant pointer id={} clientCode={} clientName={}",
                            pointer.getId(), pointer.getClientCode(), pointer.getClientName());
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                        String json = om.writeValueAsString(pointer);
                        log.info("[TenantAppPointerController] DTO JSON: {}", json);
                    } catch (Exception e) {
                        log.warn("[TenantAppPointerController] Errore serializzazione DTO: {}", e.getMessage());
                    }
                    return ResponseEntity.ok(pointer);
                })
                .orElseGet(() -> {
                    log.warn("[TenantAppPointerController] No tenant pointer found for clientCode={}", clientCode);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tenantAppPointerService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TenantDto> create(@Valid @RequestBody TenantAppPointerUpsertRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantAppPointerService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TenantDto> update(@PathVariable Long id,
                                                      @Valid @RequestBody TenantAppPointerUpsertRequestDto request) {
        return ResponseEntity.ok(tenantAppPointerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tenantAppPointerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/resolve/{clientCode}")
    public ResponseEntity<TenantResolutionDto> resolveTenantUrl(@PathVariable String clientCode,
                                                                @AuthenticationPrincipal Jwt jwt) {
        log.info("[TenantAppPointerController] Resolve URL request for clientCode={} subject={}",
                clientCode,
                jwt != null ? jwt.getSubject() : "anonymous");
        return ResponseEntity.ok(tenantAppPointerService.resolveActiveTenantUrlForJwt(clientCode, jwt));
    }
}
