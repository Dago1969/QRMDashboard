package com.qtm.dashboard;

import com.qtm.dashboard.config.DashboardSchemaPreflightInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point dell'applicazione Spring Boot per QTMDashboard.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class QtmDashboardApplication extends org.springframework.boot.web.servlet.support.SpringBootServletInitializer {

    /**
     * Avvio standalone (eseguibile jar) e deploy WAR.
     */
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(QtmDashboardApplication.class);
        application.addInitializers(new DashboardSchemaPreflightInitializer());
        application.run(args);
    }

    /**
     * Configurazione per deploy WAR su servlet container.
     */
    @Override
    protected org.springframework.boot.builder.SpringApplicationBuilder configure(org.springframework.boot.builder.SpringApplicationBuilder application) {
        return application.sources(QtmDashboardApplication.class)
                .initializers(new DashboardSchemaPreflightInitializer());
    }
}
