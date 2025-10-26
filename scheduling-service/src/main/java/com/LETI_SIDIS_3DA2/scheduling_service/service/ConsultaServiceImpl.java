package com.LETI_SIDIS_3DA2.scheduling_service.service;

import com.LETI_SIDIS_3DA2.scheduling_service.domain.Consulta;
import com.LETI_SIDIS_3DA2.scheduling_service.dto.*;
import com.LETI_SIDIS_3DA2.scheduling_service.dto.PatientDetailsDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.dto.PhysicianDetailsDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.exception.ForbiddenAccessException;
import com.LETI_SIDIS_3DA2.scheduling_service.exception.ResourceNotFoundException;
import com.LETI_SIDIS_3DA2.scheduling_service.exception.ServiceUnavailableException;
import com.LETI_SIDIS_3DA2.scheduling_service.repository.ConsultaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsultaServiceImpl implements ConsultaServiceIntf {

    private final ConsultaRepository consultaRepo;
    private final RestTemplate restTemplate;

    @Value("${services.physician.url}")
    private String physicianServiceUrl;

    @Value("${services.patient.url}")
    private String patientServiceUrl;

    @Autowired
    public ConsultaServiceImpl(ConsultaRepository consultaRepo, RestTemplate restTemplate) {
        this.consultaRepo = consultaRepo;
        this.restTemplate = restTemplate;
    }

    @Override
    @Transactional
    public ConsultaOutPutDTO create(ConsultaInputDTO dto, String patientUsername) {
        if (dto.getDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é possível marcar consultas no passado.");
        }

        PhysicianDetailsDTO physician = getPhysicianDetails(dto.getPhysicianId());
        PatientDetailsDTO patient = getPatientDetailsByUsername(patientUsername);

        // TODO: Adicionar lógica para verificar se o slot está disponível.
        // Isto usaria o método getAvailableSlots que moveremos para cá mais tarde.

        Consulta consulta = new Consulta(
                patient.getId(),
                physician.getId(),
                dto.getDateTime(),
                60, // Duração padrão, pode vir do DTO ou de uma regra de negócio
                dto.getConsultationType(),
                "SCHEDULED",
                dto.getNotes()
        );

        Consulta savedConsulta = consultaRepo.save(consulta);
        return toDto(savedConsulta);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultaOutPutDTO getById(Long id, String requesterUsername, List<String> requesterRoles) {
        Consulta consulta = consultaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada com ID: " + id));

        // Obtém detalhes do paciente associado à consulta
        PatientDetailsDTO patientOfConsulta = getPatientDetailsById(consulta.getPatientId());

        boolean isAdmin = requesterRoles.contains("ROLE_ADMIN");
        // Verifica se o username do requisitante é o mesmo do utilizador associado ao paciente da consulta
        boolean isOwner = patientOfConsulta.getUser() != null && patientOfConsulta.getUser().getUsername().equals(requesterUsername);

        if (!isAdmin && !isOwner) {
            throw new ForbiddenAccessException("Não tem permissão para aceder a esta consulta.");
        }

        return toDto(consulta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultaOutPutDTO> getByPatientUsername(String patientUsername) {
        PatientDetailsDTO patient = getPatientDetailsByUsername(patientUsername);
        return consultaRepo.findByPatientId(patient.getId()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConsultaOutPutDTO cancel(Long id, String requesterUsername, List<String> requesterRoles) {
        Consulta consulta = consultaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada com ID: " + id));

        // Lógica de autorização similar a getById
        PatientDetailsDTO patientOfConsulta = getPatientDetailsById(consulta.getPatientId());
        boolean isAdmin = requesterRoles.contains("ROLE_ADMIN");
        boolean isOwner = patientOfConsulta.getUser() != null && patientOfConsulta.getUser().getUsername().equals(requesterUsername);

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

        // Lógica de autorização
        PatientDetailsDTO patientOfConsulta = getPatientDetailsById(consulta.getPatientId());
        boolean isAdmin = requesterRoles.contains("ROLE_ADMIN");
        boolean isOwner = patientOfConsulta.getUser() != null && patientOfConsulta.getUser().getUsername().equals(requesterUsername);

        if (!isAdmin && !isOwner) {
            throw new ForbiddenAccessException("Não tem permissão para alterar esta consulta.");
        }

        // TODO: Validar se o novo slot (dto.getDateTime()) está disponível antes de alterar.

        consulta.setDateTime(dto.getDateTime());
        if (dto.getNotes() != null) {
            consulta.setNotes(dto.getNotes());
        }

        return toDto(consultaRepo.save(consulta));
    }

    // --- Métodos de Relatório (Implementação simplificada, pode precisar de otimização) ---

    @Override
    @Transactional(readOnly = true)
    public List<ConsultaOutPutDTO> getUpcomingAppointments() {
        // Precisas de um método no repositório para isto
        return consultaRepo.findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime.now()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Double> getAverageDurationPerPhysician() {
        // Esta implementação é ineficiente porque chama getPhysicianDetails para cada consulta.
        // Uma abordagem melhor seria agrupar por physicianId e depois fazer uma única chamada para obter os nomes.
        return consultaRepo.findAll().stream()
                .filter(c -> "COMPLETED".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.groupingBy(
                        c -> getPhysicianDetails(c.getPhysicianId()).getFullName(), // Ineficiente!
                        Collectors.averagingInt(Consulta::getDuration)
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getMonthlyReport(int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        List<Consulta> consultas = consultaRepo.findByDateTimeBetween(startOfMonth, endOfMonth);

        long total = consultas.size();
        long cancelled = consultas.stream()
                .filter(c -> "CANCELLED".equalsIgnoreCase(c.getStatus()))
                .count();
        long rescheduled = 0; // Mantém como 0, pois a lógica para detetar isto é mais complexa

        Map<String, Long> report = new java.util.HashMap<>();
        report.put("totalAppointments", total);
        report.put("cancelledAppointments", cancelled);
        report.put("rescheduledAppointments", rescheduled);

        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgeGroupStatsDto> getStatsByPatientAgeGroups() {
        // Esta é a implementação mais complexa e ineficiente, pois requer obter detalhes de cada paciente.
        List<Consulta> allConsultas = consultaRepo.findAll();

        // 1. Obter todos os IDs de pacientes únicos das consultas
        List<Long> patientIds = allConsultas.stream()
                .map(Consulta::getPatientId)
                .distinct()
                .toList();

        // 2. Chamar o Patient Service para obter os detalhes de todos estes pacientes de uma vez (idealmente)
        // O Patient Service precisaria de um endpoint como GET /api/patients?ids=1,2,3
        // Por agora, vamos fazer N chamadas (MUITO INEFICIENTE)
        Map<Long, PatientDetailsDTO> patientDetailsMap = patientIds.stream()
                .collect(Collectors.toMap(id -> id, this::getPatientDetailsById));

        // 3. Calcular os grupos de idade
        LocalDate today = LocalDate.now();
        return allConsultas.stream()
                .map(c -> {
                    PatientDetailsDTO patient = patientDetailsMap.get(c.getPatientId());
                    if (patient == null || patient.getBirthDateLocal() == null) return "UNKNOWN";
                    int age = Period.between(patient.getBirthDateLocal(), today).getYears();
                    if (age < 18) return "0-17";
                    else if (age < 36) return "18-35";
                    else if (age < 61) return "36-60";
                    else return "61+";
                })
                .collect(Collectors.groupingBy(
                        group -> group,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(e -> new AgeGroupStatsDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }


    // --- Métodos Helper ---

    private ConsultaOutPutDTO toDto(Consulta c) {
        PatientDetailsDTO patient = getPatientDetailsById(c.getPatientId());
        PhysicianDetailsDTO physician = getPhysicianDetails(c.getPhysicianId());

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

    private PhysicianDetailsDTO getPhysicianDetails(Long physicianId) {
        try {
            String url = physicianServiceUrl + "/" + physicianId;
            return restTemplate.getForObject(url, PhysicianDetailsDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Médico com ID " + physicianId + " não encontrado.");
        } catch (Exception e) {
            throw new ServiceUnavailableException("Serviço de Médicos indisponível no momento.");
        }
    }

    private PatientDetailsDTO getPatientDetailsById(Long patientId) {
        try {
            String url = patientServiceUrl + "/" + patientId;
            return restTemplate.getForObject(url, PatientDetailsDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Paciente com ID " + patientId + " não encontrado.");
        } catch (Exception e) {
            throw new ServiceUnavailableException("Serviço de Pacientes indisponível no momento.");
        }
    }

    private PatientDetailsDTO getPatientDetailsByUsername(String username) {
        try {
            String url = patientServiceUrl + "/by-username?username=" + username;
            return restTemplate.getForObject(url, PatientDetailsDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Paciente com username " + username + " não encontrado.");
        } catch (Exception e) {
            throw new ServiceUnavailableException("Serviço de Pacientes indisponível no momento.");
        }
    }
}