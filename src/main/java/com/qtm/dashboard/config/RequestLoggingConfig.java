package com.qtm.dashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Configurazione per il log delle richieste HTTP in ingresso.
 */
@Configuration
public class RequestLoggingConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(false);
        loggingFilter.setIncludeHeaders(false);
        loggingFilter.setAfterMessagePrefix("[HTTP REQUEST] ");
        loggingFilter.setBeforeMessagePrefix("[HTTP REQUEST] ");
        loggingFilter.setMaxPayloadLength(0);
        return loggingFilter;
    }
}
