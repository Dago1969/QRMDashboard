package com.qtm.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point dell'applicazione Spring Boot per QTMDashboard.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class QtmDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(QtmDashboardApplication.class, args);
    }
}
