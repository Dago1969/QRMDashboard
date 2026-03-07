package com.qtm.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurazione centralizzata per i parametri di integrazione con Keycloak.
 */
@ConfigurationProperties(prefix = "app.keycloak")
public class KeycloakProperties {

    private String tokenUrl;
    private String clientId;
    private String grantType;

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }
}
