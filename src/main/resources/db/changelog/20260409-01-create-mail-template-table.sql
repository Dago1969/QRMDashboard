--liquibase formatted sql
-- changeset copilot:20260409-01-create-mail-template-table
CREATE TABLE mail_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL UNIQUE,
    language VARCHAR(8) NOT NULL,
    subject VARCHAR(256) NOT NULL,
    body TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_update TIMESTAMP NOT NULL
);
CREATE INDEX idx_mail_template_code_lang ON mail_template(code, language);
