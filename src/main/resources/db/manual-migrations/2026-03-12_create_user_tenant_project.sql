-- Migration: Creazione tabella user_tenant_project per relazione User-Tenant-Progetto
CREATE TABLE user_tenant_project (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    project_id BIGINT NULL,
    superuser BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id) REFERENCES tenant_app_pointer(id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
CREATE INDEX idx_utp_user_id ON user_tenant_project(user_id);
CREATE INDEX idx_utp_tenant_id ON user_tenant_project(tenant_id);
CREATE INDEX idx_utp_project_id ON user_tenant_project(project_id);
