package com.qtm.dashboard.user.service;

import com.qtm.dashboard.user.entity.RoleEntity;
import com.qtm.dashboard.user.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service per la gestione dei ruoli applicativi e la creazione automatica se mancanti.
 */
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional
    public Set<RoleEntity> findOrCreateRoles(List<String> roleNames) {
        List<String> normalized = roleNames == null || roleNames.isEmpty()
                ? List.of("USER")
                : roleNames.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(name -> name.trim().toUpperCase(Locale.ROOT))
                .toList();

        return normalized.stream()
                .map(this::findOrCreate)
                .collect(Collectors.toSet());
    }

    private RoleEntity findOrCreate(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    RoleEntity entity = new RoleEntity();
                    entity.setName(roleName);
                    return roleRepository.save(entity);
                });
    }
}
