package com.qtm.dashboard.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

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
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/QTMDashboard?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "dago";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();
        String url = environment.getProperty("spring.datasource.url", DEFAULT_URL);
        if (url == null || !url.startsWith("jdbc:mysql:")) {
            return;
        }

        String username = environment.getProperty("spring.datasource.username", DEFAULT_USERNAME);
        String password = environment.getProperty("spring.datasource.password", DEFAULT_PASSWORD);

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
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