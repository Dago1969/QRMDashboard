
package com.qtm.dashboard.user.controller;

import com.qtm.commonlib.dto.UserRoleTenantProjectDto;
import com.qtm.dashboard.user.service.UserRoleTenantProjectService;
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
 * Controller REST centralizzato per user_role_tenant_project.
 */
@RestController
@RequestMapping("/api/user-role-tenant-project")
@RequiredArgsConstructor

public class UserRoleTenantProjectController {

    private final UserRoleTenantProjectService service;

    @GetMapping("/user/{userId}/tenant/{tenantId}")
    public ResponseEntity<List<UserRoleTenantProjectDto>> getByUserAndTenant(@PathVariable Long userId,
                                                                             @PathVariable Long tenantId) {
        return ResponseEntity.ok(service.findByUserAndTenant(userId, tenantId));
    }

    @PostMapping
    public ResponseEntity<UserRoleTenantProjectDto> create(@RequestBody UserRoleTenantProjectDto dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @DeleteMapping("/user/{userId}/tenant/{tenantId}/role/{roleId}/project/{projectId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId,
                                       @PathVariable Long tenantId,
                                       @PathVariable String roleId,
                                       @PathVariable String projectId) {
        service.delete(userId, tenantId, roleId, projectId);
        return ResponseEntity.noContent().build();
    }
}