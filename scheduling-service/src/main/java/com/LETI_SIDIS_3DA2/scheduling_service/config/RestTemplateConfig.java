package com.LETI_SIDIS_3DA2.scheduling_service.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {

        // 1) Timeouts “reais”
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(1000); // 1s para conectar
        factory.setReadTimeout(2000);    // 2s para ler resposta

        RestTemplate rt = new RestTemplate(factory);

        // 2) Interceptor: copia Authorization header da request original
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
