package com.qtm.dashboard.user.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Chiave primaria composta per UserTenantProjectRelation: tenant_id + user_id + project_id
 */
public class UserTenantProjectRelationId implements Serializable {
    private Long tenant;
    private Long user;
    private Long project;

    public UserTenantProjectRelationId() {}

    public UserTenantProjectRelationId(Long tenant, Long user, Long project) {
        this.tenant = tenant;
        this.user = user;
        this.project = project;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserTenantProjectRelationId that = (UserTenantProjectRelationId) o;
        return Objects.equals(tenant, that.tenant) &&
               Objects.equals(user, that.user) &&
               Objects.equals(project, that.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenant, user, project);
    }

    // getter e setter
    public Long getTenant() { return tenant; }
    public void setTenant(Long tenant) { this.tenant = tenant; }
    public Long getUser() { return user; }
    public void setUser(Long user) { this.user = user; }
    public Long getProject() { return project; }
    public void setProject(Long project) { this.project = project; }
}
