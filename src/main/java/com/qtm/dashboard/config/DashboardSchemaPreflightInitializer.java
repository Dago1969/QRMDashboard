package com.qtm.dashboard.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Pulisce il vecchio join table user_roles prima dell'avvio di JPA.
 * Serve per consentire la migrazione del nuovo modello ruoli con id stringa.
 */
public class DashboardSchemaPreflightInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log = LoggerFactory.getLogger(DashboardSchemaPreflightInitializer.class);
    private static final int MAX_CONNECTION_ATTEMPTS = 12;
    private static final long RETRY_DELAY_MILLIS = 5000L;

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();
        String url = environment.getProperty("spring.datasource.url");
        if (url == null || !url.startsWith("jdbc:mysql:")) {
            return;
        }

        String username = environment.getProperty("spring.datasource.username");
        String password = environment.getProperty("spring.datasource.password");

        try (Connection connection = openConnectionWithRetry(url, username, password)) {
            if (!hasLegacyUserRolesTable(connection)) {
                return;
            }

            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS user_roles");
            }
            log.warn("[QTMDashboard] Rimossa la tabella legacy user_roles per consentire la migrazione del modello ruoli centralizzato");
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossibile eseguire la pre-migrazione schema di QTMDashboard", exception);
        }
    }

    private Connection openConnectionWithRetry(String url, String username, String password) throws SQLException {
        SQLException lastException = null;

        for (int attempt = 1; attempt <= MAX_CONNECTION_ATTEMPTS; attempt++) {
            try {
                return DriverManager.getConnection(url, username, password);
            } catch (SQLException exception) {
                lastException = exception;
                log.warn("[QTMDashboard] Database non ancora pronto al tentativo {}/{}: {}", attempt, MAX_CONNECTION_ATTEMPTS, exception.getMessage());

                if (attempt == MAX_CONNECTION_ATTEMPTS) {
                    break;
                }

                sleepBeforeRetry();
            }
        }

        throw lastException != null ? lastException : new SQLException("Connessione al database non disponibile");
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(RETRY_DELAY_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interruzione durante l'attesa della disponibilita' del database", exception);
        }
    }

    private boolean hasLegacyUserRolesTable(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_roles'"
        )) {
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }
}