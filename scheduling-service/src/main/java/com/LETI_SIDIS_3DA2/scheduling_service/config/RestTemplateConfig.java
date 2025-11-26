package com.LETI_SIDIS_3DA2.scheduling_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    private final List<ClientHttpRequestInterceptor> interceptors;

    // Spring injeta todos os interceptors @Component (inclui JwtForwardingInterceptor)
    public RestTemplateConfig(List<ClientHttpRequestInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate rt = new RestTemplate();
        if (interceptors != null && !interceptors.isEmpty()) {
            rt.setInterceptors(new ArrayList<>(interceptors));
        }
        return rt;
    }
}
