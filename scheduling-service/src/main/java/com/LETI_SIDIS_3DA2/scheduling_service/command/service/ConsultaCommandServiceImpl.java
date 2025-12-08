package com.LETI_SIDIS_3DA2.scheduling_service.command.service;

import com.LETI_SIDIS_3DA2.scheduling_service.client.PatientClient;
import com.LETI_SIDIS_3DA2.scheduling_service.client.PhysicianClient;
import com.LETI_SIDIS_3DA2.scheduling_service.command.dto.ConsultaInputDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.command.dto.UpdateConsultaDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.domain.Consulta;
import com.LETI_SIDIS_3DA2.scheduling_service.exception.ForbiddenAccessException;
import com.LETI_SIDIS_3DA2.scheduling_service.exception.ResourceNotFoundException;
import com.LETI_SIDIS_3DA2.scheduling_service.messaging.ConsultationEventPayload;
import com.LETI_SIDIS_3DA2.scheduling_service.repository.ConsultaRepository;
import com.LETI_SIDIS_3DA2.scheduling_service.query.dto.ConsultaOutPutDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.query.dto.PatientDetailsDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.query.dto.PhysicianDetailsDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.messaging.ConsultationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsultaCommandServiceImpl implements ConsultaCommandService {

    private final ConsultaRepository consultaRepo;
    private final PhysicianClient physicianClient;
    private final PatientClient patientClient;
    private final ConsultationEventPublisher publisher;

    public ConsultaCommandServiceImpl(ConsultaRepository consultaRepo,
                                      PhysicianClient physicianClient,
                                      PatientClient patientClient,
                                      ConsultationEventPublisher publisher) {
        this.consultaRepo = consultaRepo;
        this.physicianClient = physicianClient;
        this.patientClient = patientClient;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public ConsultaOutPutDTO create(ConsultaInputDTO dto, String patientUsernameIgnored) {
        if (dto.getDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é possível marcar consultas no passado.");
        }

        PhysicianDetailsDTO physician = physicianClient.getById(dto.getPhysicianId());
        PatientDetailsDTO patient   = patientClient.getById(dto.getPatientId());

        // 1) criamos a consulta em estado PENDING (aguarda SAGA)
        Consulta consulta = new Consulta(
                patient.getId(),
                physician.getId(),
                dto.getDateTime(),
                60,
                dto.getConsultationType(),
                "PENDING",                 // <--- antes era "SCHEDULED"
                dto.getNotes()
        );

        Consulta saved = consultaRepo.save(consulta);

        // 2) payload mínimo para o SAGA
        ConsultationEventPayload payload = new ConsultationEventPayload(
                saved.getId(),
                saved.getPatientId(),
                saved.getPhysicianId(),
                saved.getDateTime(),
                saved.getStatus(),
                saved.getConsultationType()
        );

        // 3) evento de arranque do SAGA de marcação
        publisher.publish(
                "hap.sagas",                      // exchange de SAGA
                "consultation.requested",         // routing key
                "ConsultationRequested",          // eventType
                payload
        );

        // 4) devolvemos a consulta ainda em PENDING ao caller
        return toDto(saved, patient, physician);
    }

    @Override
    @Transactional
    public ConsultaOutPutDTO cancel(Long id, String requesterUsername, List<String> requesterRoles) {
        Consulta consulta = consultaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada com ID: " + id));

        var patientOfConsulta = patientClient.getById(consulta.getPatientId());
        boolean isAdmin = requesterRoles != null && requesterRoles.contains("ROLE_ADMIN");
        boolean isOwner = requesterUsername != null &&
                patientOfConsulta.getUser() != null &&
                requesterUsername.equals(patientOfConsulta.getUser().getUsername());

        if (!isAdmin && !isOwner) {
            throw new ForbiddenAccessException("Não tem permissão para cancelar esta consulta.");
        }

        // 1) marcamos como CANCELLATION_PENDING (à espera do SAGA)
        consulta.setStatus("CANCELLATION_PENDING");
        Consulta saved = consultaRepo.save(consulta);

        // 2) payload para o SAGA
        ConsultationEventPayload payload = new ConsultationEventPayload(
                saved.getId(),
                saved.getPatientId(),
                saved.getPhysicianId(),
                saved.getDateTime(),
                saved.getStatus(),
                saved.getConsultationType()
        );

        // 3) evento de arranque do SAGA de cancelamento
        publisher.publish(
                "hap.sagas",
                "consultation.cancellation.requested",
                "ConsultationCancellationRequested",
                payload
        );

        var physician = physicianClient.getById(saved.getPhysicianId());
        return toDto(saved, patientOfConsulta, physician);
    }

    @Override
    @Transactional
    public ConsultaOutPutDTO update(Long id, UpdateConsultaDTO dto,
                                    String requesterUsername, List<String> requesterRoles) {

        Consulta consulta = consultaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada com ID: " + id));

        var patientOfConsulta = patientClient.getById(consulta.getPatientId());
        boolean isAdmin = requesterRoles != null && requesterRoles.contains("ROLE_ADMIN");
        boolean isOwner = requesterUsername != null &&
                patientOfConsulta.getUser() != null &&
                requesterUsername.equals(patientOfConsulta.getUser().getUsername());

        if (!isAdmin && !isOwner) {
            throw new ForbiddenAccessException("Não tem permissão para alterar esta consulta.");
        }

        consulta.setDateTime(dto.getDateTime());
        if (dto.getNotes() != null) consulta.setNotes(dto.getNotes());

        Consulta saved = consultaRepo.save(consulta);

        //Publicar evento de atualização
        publisher.publish(
                "hap.consultations",
                "consultation.updated",
                "ConsultationUpdated",
                saved
        );

        var physician = physicianClient.getById(saved.getPhysicianId());
        return toDto(saved, patientOfConsulta, physician);
    }

    private ConsultaOutPutDTO toDto(Consulta c,
                                    PatientDetailsDTO patient,
                                    PhysicianDetailsDTO physician) {

        ConsultaOutPutDTO dto = new ConsultaOutPutDTO();
        dto.setId(c.getId());
        dto.setDateTime(c.getDateTime());
        dto.setDuration(c.getDuration());
        dto.setConsultationType(c.getConsultationType());
        dto.setStatus(c.getStatus());
        dto.setNotes(c.getNotes());
        dto.setPatientId(patient.getId());
        dto.setPatientName(patient.getFullName());
        dto.setPhysicianId(physician.getId());
        dto.setPhysicianName(physician.getFullName());
        return dto;
    }
}
