package com.LETI_SIDIS_3DA2.scheduling_service.service;

import com.LETI_SIDIS_3DA2.scheduling_service.client.PatientClient;
import com.LETI_SIDIS_3DA2.scheduling_service.client.PhysicianClient;
import com.LETI_SIDIS_3DA2.scheduling_service.domain.Consulta;
import com.LETI_SIDIS_3DA2.scheduling_service.dto.*;
import com.LETI_SIDIS_3DA2.scheduling_service.exception.ForbiddenAccessException;
import com.LETI_SIDIS_3DA2.scheduling_service.exception.ResourceNotFoundException;
import com.LETI_SIDIS_3DA2.scheduling_service.repository.ConsultaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsultaServiceImpl implements ConsultaServiceIntf {

    private final ConsultaRepository consultaRepo;
    private final PhysicianClient physicianClient;
    private final PatientClient patientClient;

    public ConsultaServiceImpl(ConsultaRepository consultaRepo,
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

        // ✅ usa os IDs recebidos no body; deixa de depender de username/autenticação
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
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultaOutPutDTO getById(Long id, String requesterUsername, List<String> requesterRoles) {
        Consulta consulta = consultaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada com ID: " + id));

        var patientOfConsulta = patientClient.getById(consulta.getPatientId());
        boolean isAdmin = requesterRoles != null && requesterRoles.contains("ROLE_ADMIN");
        boolean isOwner = requesterUsername != null &&
                patientOfConsulta.getUser() != null &&
                requesterUsername.equals(patientOfConsulta.getUser().getUsername());

        if (!isAdmin && !isOwner) {
            throw new ForbiddenAccessException("Não tem permissão para aceder a esta consulta.");
        }
        return toDto(consulta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultaOutPutDTO> getByPatientUsername(String patientUsername) {
        var patient = patientClient.getByUsername(patientUsername);
        return consultaRepo.findByPatientId(patient.getId()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
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
        return toDto(consultaRepo.save(consulta));
    }

    @Override
    @Transactional
    public ConsultaOutPutDTO update(Long id, UpdateConsultaDTO dto, String requesterUsername, List<String> requesterRoles) {
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

        return toDto(consultaRepo.save(consulta));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultaOutPutDTO> getUpcomingAppointments() {
        return consultaRepo.findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime.now()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Double> getAverageDurationPerPhysician() {
        return consultaRepo.findAll().stream()
                .filter(c -> "COMPLETED".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.groupingBy(
                        c -> physicianClient.getById(c.getPhysicianId()).getFullName(),
                        Collectors.averagingInt(Consulta::getDuration)
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getMonthlyReport(int year, int month) {
        var start = LocalDateTime.of(year, month, 1, 0, 0);
        var end = start.plusMonths(1).minusNanos(1);
        var consultas = consultaRepo.findByDateTimeBetween(start, end);

        long total = consultas.size();
        long cancelled = consultas.stream().filter(c -> "CANCELLED".equalsIgnoreCase(c.getStatus())).count();

        Map<String, Long> report = new java.util.HashMap<>();
        report.put("totalAppointments", total);
        report.put("cancelledAppointments", cancelled);
        report.put("rescheduledAppointments", 0L);
        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgeGroupStatsDto> getStatsByPatientAgeGroups() {
        var all = consultaRepo.findAll();
        var ids = all.stream().map(Consulta::getPatientId).distinct().toList();
        var today = LocalDate.now();

        var patientDetailsMap = ids.stream().collect(Collectors.toMap(id -> id, patientClient::getById));

        return all.stream()
                .map(c -> {
                    var p = patientDetailsMap.get(c.getPatientId());
                    if (p == null || p.getBirthDateLocal() == null) return "UNKNOWN";
                    int age = Period.between(p.getBirthDateLocal(), today).getYears();
                    if (age < 18) return "0-17";
                    else if (age < 36) return "18-35";
                    else if (age < 61) return "36-60";
                    else return "61+";
                })
                .collect(Collectors.groupingBy(g -> g, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new AgeGroupStatsDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private ConsultaOutPutDTO toDto(Consulta c) {
        var patient = patientClient.getById(c.getPatientId());
        var physician = physicianClient.getById(c.getPhysicianId());

        var dto = new ConsultaOutPutDTO();
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
