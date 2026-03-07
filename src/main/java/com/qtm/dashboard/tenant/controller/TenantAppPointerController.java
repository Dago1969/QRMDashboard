package com.qtm.dashboard.tenant.controller;

import com.qtm.dashboard.tenant.dto.TenantAppPointerDto;
import com.qtm.dashboard.tenant.dto.TenantAppPointerUpsertRequestDto;
import com.qtm.dashboard.tenant.dto.TenantResolutionDto;
import com.qtm.dashboard.tenant.service.TenantAppPointerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class TenantAppPointerController {

    private final TenantAppPointerService tenantAppPointerService;

    @GetMapping
    public ResponseEntity<List<TenantAppPointerDto>> listAll() {
        return ResponseEntity.ok(tenantAppPointerService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantAppPointerDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tenantAppPointerService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TenantAppPointerDto> create(@Valid @RequestBody TenantAppPointerUpsertRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantAppPointerService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TenantAppPointerDto> update(@PathVariable Long id,
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
        return ResponseEntity.ok(tenantAppPointerService.resolveActiveTenantUrlForJwt(clientCode, jwt));
    }
}
