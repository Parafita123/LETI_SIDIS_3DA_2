package com.LETI_SIDIS_3DA2.scheduling_service.client;

import com.LETI_SIDIS_3DA2.scheduling_service.query.dto.PatientDetailsDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.exception.ResourceNotFoundException;
import com.LETI_SIDIS_3DA2.scheduling_service.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.client.ResourceAccessException;


@Component
public class PatientClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PatientClient(RestTemplate restTemplate,
                         @Value("${services.patient.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @CircuitBreaker(name = "patientService", fallbackMethod = "fallbackGetById")
    @Retry(name = "patientService")
    public PatientDetailsDTO getById(Long patientId) {
        try {
            System.out.println("PatientClient -> chamar " + baseUrl + "/" + patientId);
            return restTemplate.getForObject(baseUrl + "/" + patientId, PatientDetailsDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Paciente com ID " + patientId + " não encontrado.");
        } catch (ResourceAccessException e) {
            // timeout / connection refused / DNS / etc
            throw new ServiceUnavailableException("Serviço de Pacientes indisponível no momento.");
        } catch (Exception e) {
            throw new ServiceUnavailableException("Serviço de Pacientes indisponível no momento.");
        }
    }

    // assinatura do fallback: mesmos params + Throwable no fim
    private PatientDetailsDTO fallbackGetById(Long patientId, Throwable t) {
        throw new ServiceUnavailableException(
                "Falha ao contactar Patient-Service (fallback) para patientId=" + patientId
        );
    }

    @CircuitBreaker(name = "patientService", fallbackMethod = "fallbackGetByUsername")
    @Retry(name = "patientService")
    public PatientDetailsDTO getByUsername(String username) {
        try {
            return restTemplate.getForObject(baseUrl + "/by-username?username={u}",
                    PatientDetailsDTO.class, username);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Paciente com username " + username + " não encontrado.");
        } catch (Exception e) {
            throw new ServiceUnavailableException("Serviço de Pacientes indisponível no momento.");
        }
    }

    private PatientDetailsDTO fallbackGetByUsername(String username, Throwable t) {
        throw new ServiceUnavailableException(
                "Falha ao contactar Patient-Service (fallback) para username=" + username
        );
    }
}
