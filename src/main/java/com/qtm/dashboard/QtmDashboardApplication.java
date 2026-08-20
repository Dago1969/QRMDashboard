package com.qtm.dashboard;

import com.qtm.dashboard.config.DashboardSchemaPreflightInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point dell'applicazione Spring Boot per QTMDashboard.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class QtmDashboardApplication extends org.springframework.boot.web.servlet.support.SpringBootServletInitializer {

    private static final Logger log = LoggerFactory.getLogger(QtmDashboardApplication.class);
    private static final String MYSQL_DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    /**
     * Avvio standalone (eseguibile jar) e deploy WAR.
     */
    public static void main(String[] args) {
        registerMySqlDriver();
        SpringApplication application = new SpringApplication(QtmDashboardApplication.class);
        application.addInitializers(new DashboardSchemaPreflightInitializer());
        application.run(args);
    }

    /**
     * Configurazione per deploy WAR su servlet container.
     */
    @Override
    protected org.springframework.boot.builder.SpringApplicationBuilder configure(org.springframework.boot.builder.SpringApplicationBuilder application) {
        registerMySqlDriver();
        return application.sources(QtmDashboardApplication.class)
                .initializers(new DashboardSchemaPreflightInitializer());
    }

    private static void registerMySqlDriver() {
        try {
            Class.forName(MYSQL_DRIVER_CLASS);
            log.debug("Driver JDBC MySQL registrato: {}", MYSQL_DRIVER_CLASS);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Driver JDBC MySQL non disponibile nel classpath: " + MYSQL_DRIVER_CLASS, exception);
        }
    }
}
