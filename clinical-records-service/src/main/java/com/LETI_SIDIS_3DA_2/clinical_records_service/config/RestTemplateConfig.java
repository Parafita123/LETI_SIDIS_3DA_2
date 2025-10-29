// Em RestTemplateConfig.java (do clinical-records-service)
package com.LETI_SIDIS_3DA_2.clinical_records_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    private final JwtForwardingInterceptor jwtForwardingInterceptor;

    public RestTemplateConfig(JwtForwardingInterceptor jwtForwardingInterceptor) {
        this.jwtForwardingInterceptor = jwtForwardingInterceptor;
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(jwtForwardingInterceptor); // Adiciona o interceptor
        return restTemplate;
    }
}