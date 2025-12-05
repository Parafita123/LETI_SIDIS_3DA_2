package com.LETI_SIDIS_3DA2.scheduling_service.command.service;

import com.LETI_SIDIS_3DA2.scheduling_service.client.PatientClient;
import com.LETI_SIDIS_3DA2.scheduling_service.client.PhysicianClient;
import com.LETI_SIDIS_3DA2.scheduling_service.command.dto.ConsultaInputDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.command.dto.UpdateConsultaDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.domain.Consulta;
import com.LETI_SIDIS_3DA2.scheduling_service.exception.ForbiddenAccessException;
import com.LETI_SIDIS_3DA2.scheduling_service.exception.ResourceNotFoundException;
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

    public ConsultaCommandServiceImpl(ConsultaRepository consultaRepo,
                                      PhysicianClient physicianClient,
                                      PatientClient patientClient) {
        this.consultaRepo = consultaRepo;
        this.physicianClient = physicianClient;
        this.patientClient = patientClient;
    }

    @Override
    @Transactional
    public ConsultaOutPutDTO create(ConsultaInputDTO dto, String patientUsernameIgnored) {
        if (dto.getDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é possível marcar consultas no passado.");
        }

        PhysicianDetailsDTO physician = physicianClient.getById(dto.getPhysicianId());
        PatientDetailsDTO patient   = patientClient.getById(dto.getPatientId());

        Consulta consulta = new Consulta(
                patient.getId(),
                physician.getId(),
                dto.getDateTime(),
                60,
                dto.getConsultationType(),
                "SCHEDULED",
                dto.getNotes()
        );

        Consulta saved = consultaRepo.save(consulta);
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

        consulta.setStatus("CANCELLED");
        Consulta saved = consultaRepo.save(consulta);
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
