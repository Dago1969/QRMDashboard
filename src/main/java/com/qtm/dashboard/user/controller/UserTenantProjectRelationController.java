package com.qtm.dashboard.user.controller;

import com.qtm.commonlib.dto.UserTenantProjectRelationDto;
import com.qtm.dashboard.user.service.UserTenantProjectRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST unico per la gestione della relazione User-Tenant-Project.
 * Permette filtro per superuser e utenti normali.
 */
@RestController
@RequestMapping("/api/user-tenant-project")
public class UserTenantProjectRelationController {
    @Autowired
    private UserTenantProjectRelationService service;

    @GetMapping("/user/{userId}")
    public List<UserTenantProjectRelationDto> getByUser(@PathVariable Long userId) {
        System.out.println("[API] GET /api/user-tenant-project/user/" + userId);
        return service.findByUserId(userId);
    }

    @GetMapping("/tenant/{tenantId}")
    public List<UserTenantProjectRelationDto> getByTenant(@PathVariable Long tenantId,
                                                         @RequestParam(required = false, defaultValue = "false") boolean onlySuperuser) {
        System.out.println("[API] GET /api/user-tenant-project/tenant/" + tenantId + "?onlySuperuser=" + onlySuperuser);
        return service.findByTenantId(tenantId, onlySuperuser);
    }

    @GetMapping("/project/{projectId}")
    public List<UserTenantProjectRelationDto> getByProject(@PathVariable Long projectId) {
        System.out.println("[API] GET /api/user-tenant-project/project/" + projectId);
        return service.findByProjectId(projectId);
    }

    @PostMapping
    public UserTenantProjectRelationDto save(@RequestBody UserTenantProjectRelationDto dto) {
        System.out.println("[API] POST /api/user-tenant-project - Salvataggio relazione: " + dto);
        return service.save(dto);
    }
}
