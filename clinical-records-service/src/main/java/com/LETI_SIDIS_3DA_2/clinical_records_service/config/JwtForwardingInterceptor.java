package com.LETI_SIDIS_3DA_2.clinical_records_service.config; // Ou .interceptor

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

@Component
public class JwtForwardingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // Obter o HttpServletRequest do contexto atual
        HttpServletRequest currentRequest = getCurrentHttpRequest();

        if (currentRequest != null) {
            // Obter o header 'Authorization' do pedido original que chegou a este serviço
            String authHeader = currentRequest.getHeader(HttpHeaders.AUTHORIZATION);

            // Se o header existir, adiciona-o ao pedido que está a sair
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                request.getHeaders().add(HttpHeaders.AUTHORIZATION, authHeader);
            }
        }

        // Continua com a execução do pedido
        return execution.execute(request, body);
    }

    private HttpServletRequest getCurrentHttpRequest() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        } catch (IllegalStateException e) {
            // Isto pode acontecer se o RestTemplate for usado fora de um contexto de pedido HTTP
            // (ex: num processo de background). Para esses casos, não há token para propagar.
            return null;
        }
    }
}