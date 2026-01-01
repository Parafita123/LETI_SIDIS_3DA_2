package com.LETI_SIDIS_3DA2.scheduling_service.command.service;

import com.LETI_SIDIS_3DA2.scheduling_service.client.PatientClient;
import com.LETI_SIDIS_3DA2.scheduling_service.client.PhysicianClient;
import com.LETI_SIDIS_3DA2.scheduling_service.command.dto.ConsultaInputDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.command.dto.UpdateConsultaDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.domain.Consulta;
import com.LETI_SIDIS_3DA2.scheduling_service.events.EventStoreAppender;
import com.LETI_SIDIS_3DA2.scheduling_service.exception.ForbiddenAccessException;
import com.LETI_SIDIS_3DA2.scheduling_service.exception.ResourceNotFoundException;
import com.LETI_SIDIS_3DA2.scheduling_service.messaging.ConsultationEventPayload;
import com.LETI_SIDIS_3DA2.scheduling_service.messaging.ConsultationEventPublisher;
import com.LETI_SIDIS_3DA2.scheduling_service.query.dto.ConsultaOutPutDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.query.dto.PatientDetailsDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.query.dto.PhysicianDetailsDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.repository.ConsultaRepository;
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
    private final EventStoreAppender eventStore;

    public ConsultaCommandServiceImpl(ConsultaRepository consultaRepo,
                                      PhysicianClient physicianClient,
                                      PatientClient patientClient,
                                      ConsultationEventPublisher publisher,
                                      EventStoreAppender eventStore) {
        this.consultaRepo = consultaRepo;
        this.physicianClient = physicianClient;
        this.patientClient = patientClient;
        this.publisher = publisher;
        this.eventStore = eventStore;
    }

    @Override
    @Transactional
    public ConsultaOutPutDTO create(ConsultaInputDTO dto, String patientUsernameIgnored) {
        if (dto.getDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é possível marcar consultas no passado.");
        }

        PhysicianDetailsDTO physician = physicianClient.getById(dto.getPhysicianId());
        PatientDetailsDTO patient = patientClient.getById(dto.getPatientId());

        // 1) cria consulta em estado PENDING
        Consulta consulta = new Consulta(
                patient.getId(),
                physician.getId(),
                dto.getDateTime(),
                60,
                dto.getConsultationType(),
                "PENDING",
                dto.getNotes()
        );

        Consulta saved = consultaRepo.save(consulta);

        // 2) payload mínimo para saga
        ConsultationEventPayload payload = new ConsultationEventPayload(
                saved.getId(),
                saved.getPatientId(),
                saved.getPhysicianId(),
                saved.getDateTime(),
                saved.getStatus(),
                saved.getConsultationType()
        );

        // 3) EVENT SOURCING: guarda evento de criação
        eventStore.append(
                "Consulta",
                saved.getId(),
                "ConsultationCreated",
                "scheduling-service",
                payload,
                null,
                null
        );

        // 4) arranque da saga de marcação
        publisher.publish(
                "hap.saga",
                "consultation.requested",
                "ConsultationRequested",
                payload
        );

        // 5) devolve PENDING
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

        // 1) marca como CANCELLATION_PENDING
        consulta.setStatus("CANCELLATION_PENDING");
        Consulta saved = consultaRepo.save(consulta);

        // 2) payload para saga
        ConsultationEventPayload payload = new ConsultationEventPayload(
                saved.getId(),
                saved.getPatientId(),
                saved.getPhysicianId(),
                saved.getDateTime(),
                saved.getStatus(),
                saved.getConsultationType()
        );

        // 3) EVENT SOURCING: guarda evento de pedido de cancelamento
        eventStore.append(
                "Consulta",
                saved.getId(),
                "ConsultationCancellationRequested",
                "scheduling-service",
                payload,
                null,
                null
        );

        // 4) publica evento de arranque da saga de cancelamento
        publisher.publish(
                "hap.saga",
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

        // evento domínio
        publisher.publish(
                "hap.consultations",
                "consultation.updated",
                "ConsultationUpdated",
                saved
        );

        // EVENT SOURCING (opcional mas bom): guarda evento de update
        eventStore.append(
                "Consulta",
                saved.getId(),
                "ConsultationUpdated",
                "scheduling-service",
                saved,
                null,
                null
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
