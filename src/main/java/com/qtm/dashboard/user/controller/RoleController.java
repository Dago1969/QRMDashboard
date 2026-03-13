package com.qtm.dashboard.user.controller;

import com.qtm.dashboard.user.dto.RoleDeleteCheckDto;
import com.qtm.commonlib.dto.RoleDto;
import com.qtm.dashboard.user.service.RoleService;
import lombok.RequiredArgsConstructor;
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
 * Controller REST CRUD ruoli centralizzati per TENAPP.
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<RoleDto> create(@RequestBody RoleDto roleDto) {
        return ResponseEntity.ok(roleService.create(roleDto));
    }

    @GetMapping
    public ResponseEntity<List<com.qtm.commonlib.dto.RoleDto>> findAll() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDto> findById(@PathVariable String id) {
        return ResponseEntity.ok(roleService.findById(id));
    }

    @GetMapping("/delete-check/{id}")
    public ResponseEntity<RoleDeleteCheckDto> getDeleteCheck(@PathVariable String id) {
        return ResponseEntity.ok(roleService.getDeleteCheck(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleDto> update(@PathVariable String id, @RequestBody RoleDto roleDto) {
        return ResponseEntity.ok(roleService.update(id, roleDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, @RequestParam(required = false) String replacementRoleId) {
        roleService.delete(id, replacementRoleId);
        return ResponseEntity.noContent().build();
    }
}
