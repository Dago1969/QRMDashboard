package com.qtm.dashboard.tenant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Proprieta di default per il bootstrap dei puntamenti tenant app.
 */
@ConfigurationProperties(prefix = "app.tenants")
@Getter
@Setter
public class TenantRoutingDefaultsProperties {

    private String defaultTenantAppUrl;
    private List<TenantMapping> mappings = new ArrayList<>();

    @Getter
    @Setter
    public static class TenantMapping {
        private String clientCode;
        private String clientName;
        private String tenantAppUrl;
        private Boolean enabled;
    }
}
