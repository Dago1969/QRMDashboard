package com.qtm.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * Configurazione centralizzata per i parametri di integrazione con Keycloak.
 */
@ConfigurationProperties(prefix = "app.keycloak")
public class KeycloakProperties {

    private String serverUrl;
    private String realm;
    private String realmCode;
    private String tokenUrl;
    private String clientId;
    private String grantType;
    private String clientSecret;
    private String adminClientId;
    private String adminClientSecret;
    private String adminGrantType;
    private String adminUsername;
    private String adminPassword;
    private String adminUserRealm;
    private String clientAssociationAttribute;

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getRealm() {
        return StringUtils.hasText(realm) ? realm : realmCode;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getRealmCode() {
        return realmCode;
    }

    public void setRealmCode(String realmCode) {
        this.realmCode = realmCode;
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

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getAdminUserRealm() {
        return adminUserRealm;
    }

    public void setAdminUserRealm(String adminUserRealm) {
        this.adminUserRealm = adminUserRealm;
    }

    public String getClientAssociationAttribute() {
        return clientAssociationAttribute;
    }

    public void setClientAssociationAttribute(String clientAssociationAttribute) {
        this.clientAssociationAttribute = clientAssociationAttribute;
    }
}
