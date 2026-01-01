package com.LETI_SIDIS_3DA2.scheduling_service.client;

import com.LETI_SIDIS_3DA2.scheduling_service.query.dto.PhysicianDetailsDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.exception.ResourceNotFoundException;
import com.LETI_SIDIS_3DA2.scheduling_service.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Component
public class PhysicianClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PhysicianClient(RestTemplate restTemplate,
                           @Value("${services.physician.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @CircuitBreaker(name = "physicianService", fallbackMethod = "fallbackGetById")
    @Retry(name = "physicianService")
    public PhysicianDetailsDTO getById(Long physicianId) {
        try {
            return restTemplate.getForObject(baseUrl + "/" + physicianId, PhysicianDetailsDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Médico com ID " + physicianId + " não encontrado.");
        } catch (Exception e) {
            throw new ServiceUnavailableException("Serviço de Médicos indisponível no momento.");
        }
    }

    private PhysicianDetailsDTO fallbackGetById(Long physicianId, Throwable t) {
        throw new ServiceUnavailableException(
                "Falha ao contactar Physician-Service (fallback) para physicianId=" + physicianId
        );
    }
}
