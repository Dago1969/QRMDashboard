package com.qtm.dashboard.user.controller;

import com.qtm.commonlib.dto.UserTenantRoleRelationDto;
import com.qtm.dashboard.user.service.UserTenantRoleRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller di compatibilita per le API user-tenant-role richieste da TENAPP.
 */
@RestController
@RequestMapping("/api/user-tenant-role")
@RequiredArgsConstructor
public class UserTenantRoleRelationController {

    private final UserTenantRoleRelationService service;

    @GetMapping("/user/{userId}/tenant/{tenantId}")
    public ResponseEntity<List<UserTenantRoleRelationDto>> getByUserAndTenant(@PathVariable Long userId,
                                                                               @PathVariable Long tenantId) {
        return ResponseEntity.ok(service.findByUserAndTenant(userId, tenantId));
    }

    @PostMapping
    public ResponseEntity<UserTenantRoleRelationDto> create(@RequestBody UserTenantRoleRelationDto dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}