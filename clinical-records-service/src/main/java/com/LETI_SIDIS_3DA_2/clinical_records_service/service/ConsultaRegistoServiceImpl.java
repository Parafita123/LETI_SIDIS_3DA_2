package com.LETI_SIDIS_3DA_2.clinical_records_service.service;

import com.LETI_SIDIS_3DA_2.clinical_records_service.domain.ConsultaRegisto;
import com.LETI_SIDIS_3DA_2.clinical_records_service.dto.CreateConsultaRegistoDTO;
import com.LETI_SIDIS_3DA_2.clinical_records_service.dto.ConsultaRegistoOutputDTO;
import com.LETI_SIDIS_3DA_2.clinical_records_service.dto.AppointmentDetailsDTO;
import com.LETI_SIDIS_3DA_2.clinical_records_service.exception.DuplicateResourceException;
import com.LETI_SIDIS_3DA_2.clinical_records_service.exception.ResourceNotFoundException;
import com.LETI_SIDIS_3DA_2.clinical_records_service.exception.ServiceUnavailableException;
import com.LETI_SIDIS_3DA_2.clinical_records_service.repository.ConsultaRegistoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ConsultaRegistoServiceImpl implements ConsultaRegistoService { // Garante que o nome da interface aqui está correto

    private final ConsultaRegistoRepository recordRepository;
    private final RestTemplate restTemplate;

    @Value("${services.scheduling.url}")
    private String schedulingServiceUrl;

    @Autowired
    public ConsultaRegistoServiceImpl(ConsultaRegistoRepository recordRepository, RestTemplate restTemplate) {
        this.recordRepository = recordRepository;
        this.restTemplate = restTemplate;
    }

    // --- IMPLEMENTAÇÃO DOS MÉTODOS DA INTERFACE ---

    @Override // Adiciona @Override
    @Transactional
    public ConsultaRegistoOutputDTO createRecord(CreateConsultaRegistoDTO dto) {
        AppointmentDetailsDTO appointment = getAppointmentDetails(dto.getConsultaId());

        if (!"COMPLETED".equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalArgumentException("Só é possível criar registos para consultas com o estado 'COMPLETED'. O estado atual é: " + appointment.getStatus());
        }

        if (recordRepository.findByConsultaId(dto.getConsultaId()).isPresent()) {
            throw new DuplicateResourceException("Já existe um registo para a consulta com ID: " + dto.getConsultaId());
        }

        ConsultaRegisto record = new ConsultaRegisto(
                dto.getConsultaId(),
                dto.getDiagnosis(),
                dto.getTreatmentRecommendations(),
                dto.getPrescriptions(),
                LocalDateTime.now()
        );

        ConsultaRegisto savedRecord = recordRepository.save(record);
        return convertToDTO(savedRecord);
    }

    @Override // Adiciona @Override
    @Transactional(readOnly = true)
    public Optional<ConsultaRegistoOutputDTO> getRecordByConsultaId(Long consultaId) {
        return recordRepository.findByConsultaId(consultaId)
                .map(this::convertToDTO); // Chama o método convertToDTO
    }

    // --- MÉTODO HELPER 'convertToDTO' (EM FALTA) ---

    private ConsultaRegistoOutputDTO convertToDTO(ConsultaRegisto record) {
        if (record == null) {
            return null;
        }

        ConsultaRegistoOutputDTO dto = new ConsultaRegistoOutputDTO();
        dto.setId(record.getId());
        dto.setConsultaId(record.getConsultaId());
        dto.setDiagnosis(record.getDiagnosis());
        dto.setTreatmentRecommendations(record.getTreatmentRecommendations());
        dto.setPrescriptions(record.getPrescriptions());
        dto.setCreatedAt(record.getCreatedAt());

        return dto;
    }

    // --- MÉTODO HELPER para chamar o Scheduling Service (já tinhas) ---
    private AppointmentDetailsDTO getAppointmentDetails(Long consultaId) {
        try {
            String url = schedulingServiceUrl + "/" + consultaId;
            AppointmentDetailsDTO appointmentDetails = restTemplate.getForObject(url, AppointmentDetailsDTO.class);

            if (appointmentDetails == null) {
                throw new ResourceNotFoundException("Consulta com ID " + consultaId + " não encontrada no serviço de agendamento (resposta vazia).");
            }
            return appointmentDetails;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Consulta com ID " + consultaId + " não encontrada no serviço de agendamento.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceUnavailableException("Serviço de Agendamento (Scheduling Service) indisponível no momento.");
        }
    }
}