package com.LETI_SIDIS_3DA_2.clinical_records_service.command.service;

import com.LETI_SIDIS_3DA_2.clinical_records_service.domain.ConsultaRegisto;
import com.LETI_SIDIS_3DA_2.clinical_records_service.query.dto.AppointmentDetailsDTO;
import com.LETI_SIDIS_3DA_2.clinical_records_service.query.dto.ConsultaRegistoOutputDTO;
import com.LETI_SIDIS_3DA_2.clinical_records_service.command.dto.CreateConsultaRegistoDTO;
import com.LETI_SIDIS_3DA_2.clinical_records_service.exception.DuplicateResourceException;
import com.LETI_SIDIS_3DA_2.clinical_records_service.exception.ResourceNotFoundException;
import com.LETI_SIDIS_3DA_2.clinical_records_service.exception.ServiceUnavailableException;
import com.LETI_SIDIS_3DA_2.clinical_records_service.repository.ConsultaRegistoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.LETI_SIDIS_3DA_2.clinical_records_service.messaging.ClinicalRecordEventPublisher;
import java.time.LocalDateTime;

@Service
public class ConsultaRegistoCommandServiceImpl implements ConsultaRegistoCommandService {

    private final ConsultaRegistoRepository recordRepository;
    private final RestTemplate restTemplate;


    @Value("${services.scheduling.url}")
    private String schedulingServiceUrl;
    private final ClinicalRecordEventPublisher eventPublisher;


    public ConsultaRegistoCommandServiceImpl(ConsultaRegistoRepository recordRepository,
                                             RestTemplate restTemplate, ClinicalRecordEventPublisher eventPublisher) {
        this.recordRepository = recordRepository;
        this.restTemplate = restTemplate;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ConsultaRegistoOutputDTO createRecord(CreateConsultaRegistoDTO dto) {
        AppointmentDetailsDTO appointment = getAppointmentDetails(dto.getConsultaId());

        if (!"COMPLETED".equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalArgumentException(
                    "Só é possível criar registos para consultas com o estado 'COMPLETED'. " +
                            "O estado atual é: " + appointment.getStatus()
            );
        }

        if (recordRepository.findByConsultaId(dto.getConsultaId()).isPresent()) {
            throw new DuplicateResourceException(
                    "Já existe um registo para a consulta com ID: " + dto.getConsultaId()
            );
        }

        ConsultaRegisto record = new ConsultaRegisto(
                dto.getConsultaId(),
                dto.getDiagnosis(),
                dto.getTreatmentRecommendations(),
                dto.getPrescriptions(),
                LocalDateTime.now()
        );

        ConsultaRegisto saved = recordRepository.save(record);
        // publicar evento
        eventPublisher.publishClinicalRecordCreated(saved);

        return convertToDTO(saved);
    }

    private ConsultaRegistoOutputDTO convertToDTO(ConsultaRegisto record) {
        if (record == null) return null;

        ConsultaRegistoOutputDTO dto = new ConsultaRegistoOutputDTO();
        dto.setId(record.getId());
        dto.setConsultaId(record.getConsultaId());
        dto.setDiagnosis(record.getDiagnosis());
        dto.setTreatmentRecommendations(record.getTreatmentRecommendations());
        dto.setPrescriptions(record.getPrescriptions());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }

    private AppointmentDetailsDTO getAppointmentDetails(Long consultaId) {
        try {
            String url = schedulingServiceUrl + "/" + consultaId;
            AppointmentDetailsDTO appointmentDetails =
                    restTemplate.getForObject(url, AppointmentDetailsDTO.class);

            if (appointmentDetails == null) {
                throw new ResourceNotFoundException(
                        "Consulta com ID " + consultaId +
                                " não encontrada no serviço de agendamento (resposta vazia)."
                );
            }
            return appointmentDetails;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException(
                    "Consulta com ID " + consultaId + " não encontrada no serviço de agendamento."
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceUnavailableException(
                    "Serviço de Agendamento (Scheduling Service) indisponível no momento."
            );
        }
    }
}
