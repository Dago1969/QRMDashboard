package com.qtm.dashboard.tenant.config;

import com.qtm.dashboard.config.KeycloakProperties;
import com.qtm.dashboard.tenant.entity.TenantAppPointerEntity;
import com.qtm.dashboard.tenant.repository.TenantAppPointerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Popola in modo idempotente la tabella dei puntamenti con i valori correnti di configurazione.
 */
@Component
@RequiredArgsConstructor
public class TenantAppPointerDataInitializer implements ApplicationRunner {

    private final TenantAppPointerRepository tenantAppPointerRepository;
    private final KeycloakProperties keycloakProperties;
    private final TenantRoutingDefaultsProperties tenantRoutingDefaultsProperties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!tenantRoutingDefaultsProperties.getMappings().isEmpty()) {
            tenantRoutingDefaultsProperties.getMappings().forEach(this::upsertFromMapping);
            return;
        }

        String fallbackClientCode = keycloakProperties.getClientId();
        if (fallbackClientCode == null || fallbackClientCode.isBlank()) {
            return;
        }

        upsert(
                fallbackClientCode,
                fallbackClientCode,
                resolveTenantAppUrl(null),
                true
        );
    }

    private void upsertFromMapping(TenantRoutingDefaultsProperties.TenantMapping mapping) {
        if (mapping == null || mapping.getClientCode() == null || mapping.getClientCode().isBlank()) {
            return;
        }

        upsert(
                mapping.getClientCode(),
                resolveClientName(mapping.getClientName(), mapping.getClientCode()),
                resolveTenantAppUrl(mapping.getTenantAppUrl()),
                mapping.getEnabled() == null || mapping.getEnabled()
        );
    }

    private void upsert(String clientCode, String clientName, String tenantAppUrl, boolean enabled) {
        var existing = tenantAppPointerRepository.findByClientCode(clientCode);
        if (existing.isPresent()) {
            TenantAppPointerEntity current = existing.get();

            // Backward-compatibility: convert legacy backend URL defaults to frontend URL.
            if (isLegacyDefaultBackendUrl(current.getTenantAppUrl())
                    && tenantAppUrl != null
                    && !tenantAppUrl.isBlank()) {
                current.setTenantAppUrl(tenantAppUrl);
                current.setClientName(clientName);
                current.setEnabled(enabled);
                tenantAppPointerRepository.save(current);
            }
            return;
        }

        tenantAppPointerRepository.save(
            TenantAppPointerEntity.builder()
                .clientCode(clientCode)
                .clientName(clientName)
                .tenantAppUrl(tenantAppUrl)
                .enabled(enabled)
                .build()
        );
    }

    private boolean isLegacyDefaultBackendUrl(String currentUrl) {
        return "http://localhost:8087".equals(currentUrl)
                || "http://localhost:8087/".equals(currentUrl);
    }

    private String resolveClientName(String configuredName, String fallbackClientCode) {
        return configuredName == null || configuredName.isBlank() ? fallbackClientCode : configuredName;
    }

    private String resolveTenantAppUrl(String configuredUrl) {
        if (configuredUrl != null && !configuredUrl.isBlank()) {
            return configuredUrl;
        }
        configuredUrl = tenantRoutingDefaultsProperties.getDefaultTenantAppUrl();
        return configuredUrl == null || configuredUrl.isBlank() ? "http://localhost:8087" : configuredUrl;
    }
}
