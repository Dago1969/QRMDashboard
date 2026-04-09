-- Inserimento template onboarding (italiano)
INSERT INTO mail_template (code, language, subject, body, enabled, last_update) VALUES (
    'ONBOARDING', 'it', 'Benvenuto su QTM',
    'Ciao ${firstName},\n\nIl tuo account è stato creato. Username: ${username}\nAccedi qui: ${loginUrl}\n\nCordiali saluti,\nIl team QTM',
    TRUE, NOW()
);

-- Inserimento template onboarding (inglese)
INSERT INTO mail_template (code, language, subject, body, enabled, last_update) VALUES (
    'ONBOARDING', 'en', 'Welcome to QTM',
    'Hi ${firstName},\n\nYour account has been created. Username: ${username}\nLogin here: ${loginUrl}\n\nBest regards,\nThe QTM Team',
    TRUE, NOW()
);
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
