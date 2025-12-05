package com.LETI_SIDIS_3DA2.scheduling_service.client;

import com.LETI_SIDIS_3DA2.scheduling_service.query.dto.PatientDetailsDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.exception.ResourceNotFoundException;
import com.LETI_SIDIS_3DA2.scheduling_service.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class PatientClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PatientClient(RestTemplate restTemplate,
                         @Value("${services.patient.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public PatientDetailsDTO getById(Long patientId) {
        try {
            return restTemplate.getForObject(baseUrl + "/" + patientId, PatientDetailsDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Paciente com ID " + patientId + " não encontrado.");
        } catch (Exception e) {
            throw new ServiceUnavailableException("Serviço de Pacientes indisponível no momento.");
        }
    }

    public PatientDetailsDTO getByUsername(String username) {
        try {
            return restTemplate.getForObject(baseUrl + "/by-username?username={u}", PatientDetailsDTO.class, username);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Paciente com username " + username + " não encontrado.");
        } catch (Exception e) {
            throw new ServiceUnavailableException("Serviço de Pacientes indisponível no momento.");
        }
    }
}
