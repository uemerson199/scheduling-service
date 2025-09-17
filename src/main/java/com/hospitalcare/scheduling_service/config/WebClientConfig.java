package com.hospitalcare.scheduling_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${patient.service.url}")
    private String patientServiceUrl;

    @Bean
    public WebClient patientServiceWebClient() {
        return WebClient.builder()
                .baseUrl(patientServiceUrl)
                .build();
    }
}