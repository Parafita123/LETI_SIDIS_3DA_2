package com.LETI_SIDIS_3DA2.scheduling_service.client;

import com.LETI_SIDIS_3DA2.scheduling_service.dto.PhysicianDetailsDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.exception.ResourceNotFoundException;
import com.LETI_SIDIS_3DA2.scheduling_service.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class PhysicianClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PhysicianClient(RestTemplate restTemplate,
                           @Value("${services.physician.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public PhysicianDetailsDTO getById(Long physicianId) {
        try {
            return restTemplate.getForObject(baseUrl + "/" + physicianId, PhysicianDetailsDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Médico com ID " + physicianId + " não encontrado.");
        } catch (Exception e) {
            throw new ServiceUnavailableException("Serviço de Médicos indisponível no momento.");
        }
    }
}
