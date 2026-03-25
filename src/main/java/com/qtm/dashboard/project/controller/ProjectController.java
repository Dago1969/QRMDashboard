package com.qtm.dashboard.project.controller;

import com.qtm.commonlib.dto.ProjectDto;
import com.qtm.dashboard.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST CRUD dei progetti centralizzati da consumare da TENANTS-APP.
 */
@RestController
@RequestMapping("/api/projects")
@Slf4j
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectDto> create(@RequestBody ProjectDto projectDto) {
        log.info("Ricevuta richiesta creazione project centralizzato: code={}, tenant={}, tenantId={}",
            projectDto.getCode(), projectDto.getTenant(), projectDto.getTenantId());
        log.debug("[ProjectController] Payload ProjectDto: {}", projectDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(projectDto));
    }

    @GetMapping
    public ResponseEntity<List<ProjectDto>> findAll(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String tenant
    ) {
        log.info("[API] GET /api/projects - Ricerca project centralizzati richiesta: code={}, tenant={}", code, tenant);
        List<ProjectDto> result = projectService.findAll(code, tenant);
        log.info("[API] GET /api/projects - Trovati {} progetti", result.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> findById(@PathVariable Long id) {
        log.debug("Lettura project centralizzato richiesta: id={}", id);
        return ResponseEntity.ok(projectService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> update(@PathVariable Long id, @RequestBody ProjectDto projectDto) {
        log.info("Ricevuta richiesta aggiornamento project centralizzato: id={}, code={}, tenant={}, tenantId={}",
                id, projectDto.getCode(), projectDto.getTenant(), projectDto.getTenantId());
        return ResponseEntity.ok(projectService.update(id, projectDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Ricevuta richiesta eliminazione project centralizzato: id={}", id);
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}