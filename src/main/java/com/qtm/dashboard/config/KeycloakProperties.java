package com.qtm.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurazione centralizzata per i parametri di integrazione con Keycloak.
 */
@ConfigurationProperties(prefix = "app.keycloak")
public class KeycloakProperties {

    private String serverUrl;
    private String realm;
    private String tokenUrl;
    private String clientId;
    private String grantType;
    private String clientSecret;
    private String adminClientId;
    private String adminClientSecret;
    private String adminGrantType;
    private String clientAssociationAttribute;

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

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

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAdminClientId() {
        return adminClientId;
    }

    public void setAdminClientId(String adminClientId) {
        this.adminClientId = adminClientId;
    }

    public String getAdminClientSecret() {
        return adminClientSecret;
    }

    public void setAdminClientSecret(String adminClientSecret) {
        this.adminClientSecret = adminClientSecret;
    }

    public String getAdminGrantType() {
        return adminGrantType;
    }

    public void setAdminGrantType(String adminGrantType) {
        this.adminGrantType = adminGrantType;
    }

    public String getClientAssociationAttribute() {
        return clientAssociationAttribute;
    }

    public void setClientAssociationAttribute(String clientAssociationAttribute) {
        this.clientAssociationAttribute = clientAssociationAttribute;
    }
}
