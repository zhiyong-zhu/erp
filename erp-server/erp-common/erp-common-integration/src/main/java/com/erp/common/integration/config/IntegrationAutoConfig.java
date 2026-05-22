package com.erp.common.integration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableRetry
public class IntegrationAutoConfig {

    @Bean
    public WebClient integrationWebClient(WebClient.Builder builder) {
        return builder.build();
    }
}
