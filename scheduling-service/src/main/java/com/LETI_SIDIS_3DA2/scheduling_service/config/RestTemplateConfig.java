package com.LETI_SIDIS_3DA2.scheduling_service.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.ArrayList;
import java.util.List;

    @Configuration
    public class RestTemplateConfig {

        @Bean
        public RestTemplate restTemplate() {
            RestTemplate rt = new RestTemplate();

            // Interceptor que copia o Authorization header da request original
            rt.getInterceptors().add((request, body, execution) -> {
                ServletRequestAttributes attrs =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                if (attrs != null) {
                    HttpServletRequest currentRequest = attrs.getRequest();
                    String authHeader = currentRequest.getHeader("Authorization");
                    if (authHeader != null && !authHeader.isBlank()) {
                        request.getHeaders().add("Authorization", authHeader);
                    }
                }

                return execution.execute(request, body);
            });

            return rt;
        }
    }
