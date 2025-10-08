package com.psoft2024._5.grupo1.projeto_psoft.service;

import com.psoft2024._5.grupo1.projeto_psoft.domain.*;
import com.psoft2024._5.grupo1.projeto_psoft.dto.*;
import com.psoft2024._5.grupo1.projeto_psoft.exception.*;
import com.psoft2024._5.grupo1.projeto_psoft.repository.ConsultasRepository;
import com.psoft2024._5.grupo1.projeto_psoft.repository.*;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.time.DayOfWeek;
import com.psoft2024._5.grupo1.projeto_psoft.dto.AgeGroupStatsDto;
import java.time.Period;
import java.time.LocalDate;
@Service
public class ConsultaService implements  ConsultaServiceIntf{

    private final ConsultasRepository consultaRepo;
    private final PatientRepository patientRepo;
    private final PhysicianRepository physicianRepo;

    public ConsultaService(ConsultasRepository consultaRepo, PatientRepository patientRepo, PhysicianRepository physicianRepo) {
        this.consultaRepo = consultaRepo;
        this.patientRepo = patientRepo;
        this.physicianRepo = physicianRepo;
    }
    @Override
    public ConsultaOutPutDTO create(ConsultaDTO dto) {
        validateDateTime(dto.getDateTime());

        Physician physician = physicianRepo.findById(dto.getPhysicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Physician not found"));

        if (consultaRepo.findByPhysicianAndDateTime(physician, dto.getDateTime()).isPresent())
            throw new IllegalArgumentException("Physician is not available at this time.");

        Patient patient = patientRepo.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Consulta consulta = new Consulta(patient, physician, dto.getDateTime(), 20,
                dto.getConsultationType(), "SCHEDULED", dto.getNotes());

        return toDto(consultaRepo.save(consulta));
    }
    @Override
    public List<ConsultaOutPutDTO> getByPatient(Patient patient) {
        return consultaRepo.findByPatient(patient)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    @Override
    public ConsultaOutPutDTO getById(Long id) {
        Consulta consulta = consultaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta not found"));
        return toDto(consulta);
    }
    @Override
    public ConsultaOutPutDTO cancel(Long id) {
        Consulta consulta = consultaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta not found"));
        consulta.setStatus("CANCELLED");
        return toDto(consultaRepo.save(consulta));
    }
    @Override
    public ConsultaOutPutDTO update(Long id, ConsultaOutPutDTO dto) {
        Consulta consulta = consultaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta not found"));

        if (dto.getDateTime() != null) {
            validateDateTime(dto.getDateTime());

            if (!dto.getDateTime().equals(consulta.getDateTime()) &&
                    consultaRepo.findByPhysicianAndDateTime(consulta.getPhysician(), dto.getDateTime()).isPresent()) {
                throw new IllegalArgumentException("Physician is not available at that time.");
            }

            consulta.setDateTime(dto.getDateTime());
        }

        if (dto.getConsultationType() != null) consulta.setConsultationType(dto.getConsultationType());
        if (dto.getNotes() != null) consulta.setNotes(dto.getNotes());

        return toDto(consultaRepo.save(consulta));
    }

    private ConsultaOutPutDTO toDto(Consulta c) {
        return new ConsultaOutPutDTO(
                c.getId(),
                c.getPatient().getName(),
                c.getPhysician().getFullName(),
                c.getDateTime(),
                c.getDuration(),
                c.getConsultationType(),
                c.getStatus(),
                c.getNotes()
        );
    }

    private void validateDateTime(LocalDateTime dt) {
        DayOfWeek day = dt.getDayOfWeek();
        int hour = dt.getHour();
        int minute = dt.getMinute();

        if (day == DayOfWeek.SUNDAY)
            throw new IllegalArgumentException("Consultas não são permitidas aos domingos.");

        if (minute % 20 != 0)
            throw new IllegalArgumentException("As marcações devem respeitar intervalos de 20 minutos.");

        boolean sabado = day == DayOfWeek.SATURDAY;
        if (sabado && (hour < 9 || hour >= 13))
            throw new IllegalArgumentException("Horário de sábado: 9h às 13h.");

        if (!sabado && (hour < 9 || (hour >= 13 && hour < 14) || hour >= 20))
            throw new IllegalArgumentException("Horário de segunda a sexta: 9h–13h e 14h–20h.");
    }
    @Override
    public List<ConsultaOutPutDTO> getUpcomingAppointments() {
        return consultaRepo.findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime.now())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Double> getAverageDurationPerPhysician() {
        return consultaRepo.findAll().stream()
                .filter(c -> c.getStatus().equalsIgnoreCase("SCHEDULED") || c.getStatus().equalsIgnoreCase("COMPLETED"))
                .collect(Collectors.groupingBy(
                        c -> c.getPhysician().getFullName(),
                        Collectors.averagingInt(Consulta::getDuration)
                ));
    }
    @Override
    public ConsultasReportDTO getMonthlyReport(int year, int month) {
        List<Consulta> consultas = consultaRepo.findAll().stream()
                .filter(c -> c.getDateTime().getYear() == year && c.getDateTime().getMonthValue() == month)
                .toList();

        long total = consultas.size();
        long cancelled = consultas.stream().filter(c -> c.getStatus().equalsIgnoreCase("CANCELLED")).count();
        long rescheduled = consultas.stream().filter(c -> c.getStatus().equalsIgnoreCase("RESCHEDULED")).count();

        return new ConsultasReportDTO(year, month, total, cancelled, rescheduled);
    }
    @Override
    public List<AgeGroupStatsDto> getStatsByPatientAgeGroups() {
        LocalDate today = LocalDate.now();
        return consultaRepo.findAll().stream()
                .map(c -> {
                    int age = Period.between(c.getPatient().getBirthDateLocal(), today).getYears();
                    if (age < 18)       return "0-17";
                    else if (age < 36)  return "18-35";
                    else if (age < 61)  return "36-60";
                    else                return "61+";
                })
                .collect(Collectors.groupingBy(
                        group -> group,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(e -> new AgeGroupStatsDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
}
