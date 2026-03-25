-- Script SQL per nuova tabella user_tenant_project con chiave primaria composta
DROP TABLE IF EXISTS user_tenant_project;

CREATE TABLE user_tenant_project (
    tenant_id bigint NOT NULL,
    user_id bigint NOT NULL,
    project_id bigint NOT NULL,
    superuser bit(1) DEFAULT NULL,
    email varchar(255) DEFAULT NULL,
    PRIMARY KEY (tenant_id, user_id, project_id),
    KEY FK_project (project_id),
    KEY FK_tenant (tenant_id),
    KEY FK_user (user_id),
    CONSTRAINT FK_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT FK_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT FK_tenant FOREIGN KEY (tenant_id) REFERENCES tenant_app_pointer (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
