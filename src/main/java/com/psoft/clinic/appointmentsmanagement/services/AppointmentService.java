package com.psoft.clinic.appointmentsmanagement.services;

import com.psoft.clinic.appointmentsmanagement.model.Appointment;
import com.psoft.clinic.appointmentsmanagement.model.AppointmentStatus;
import com.psoft.clinic.appointmentsmanagement.model.ConsultationType;
import com.psoft.clinic.appointmentsmanagement.repository.SpringBootAppointmentRepository;
import com.psoft.clinic.appointmentsmanagement.model.AppointmentDateTimeValidator;
import com.psoft.clinic.exceptions.InvalidAppointmentException;
import com.psoft.clinic.exceptions.InvalidDateException;
import com.psoft.clinic.model.DailySlots;
import com.psoft.clinic.patientmanagement.model.Patient;
import com.psoft.clinic.patientmanagement.repository.SpringBootPatientRepository;
import com.psoft.clinic.physiciansmanagement.model.Physician;
import com.psoft.clinic.physiciansmanagement.repository.SpringBootPhysicianRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.time.LocalDate;
import java.time.LocalTime;

import static ch.qos.logback.core.util.StringUtil.capitalizeFirstLetter;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final SpringBootAppointmentRepository appointmentRepository;
    private final SpringBootPatientRepository     patientRepository;
    private final SpringBootPhysicianRepository   physicianRepository;
    private final AppointmentDateTimeValidator    validator;

    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public boolean exists(
            LocalDate date,
            LocalTime startTime,
            String patientFullName,
            String physicianFullName
    ) {
        return appointmentRepository
                .checkExists(date, startTime, patientFullName, physicianFullName);
    }


    public Appointment create(CreateAppointmentRequest request) {
        LocalDate date;
        try {
            date = LocalDate.parse(request.getDate(), DATE_FMT);
        } catch (DateTimeParseException ex) {
            throw new InvalidAppointmentException(
                    "Formato de data inválido (use dd-MM-yyyy): " + request.getDate()
            );
        }
        date = AppointmentDateTimeValidator.validateDate(date);

        LocalTime start;
        try {
            start = LocalTime.parse(request.getStartTime(), TIME_FMT);
        } catch (DateTimeParseException ex) {
            throw new InvalidAppointmentException(
                    "Formato de hora inválido (use HH:mm): " + request.getStartTime()
            );
        }
        LocalTime end = start.plusMinutes(20);
        validator.validateTimeSlot(date, start, end);

        Appointment ap = new Appointment();
        ap.setDate(date);
        ap.setStartTime(start);
        ap.setEndTime(end);
        ap.setConsultationType(ConsultationType.valueOf(request.getConsultationType()));

        ap.setPatientFullName(request.getPatientFullName());
        Optional<Patient> opt = patientRepository.findByBaseUserFullName(request.getPatientFullName());
        ap.setPatient(opt.orElse(null));

        ap.setPhysicianFullName(request.getPhysicianFullName());
        Physician physician = physicianRepository
                .findByBaseUserFullName(request.getPhysicianFullName())
                .orElseThrow(() ->
                        new EntityNotFoundException("Médico não encontrado: " + request.getPhysicianFullName())
                );
        ap.setPhysician(physician);
        ap.setStatus(AppointmentStatus.SCHEDULED);

        if (appointmentRepository.existsByPhysicianAndDateAndStartTime(physician, date, start)) {
            throw new InvalidAppointmentException(
                    "Conflito: o médico " + request.getPhysicianFullName() +" já possui consulta às " + request.getStartTime()
            );
        }
        ap.setDetails(request.getDetails());
        return appointmentRepository.save(ap);
    }

    public List<DailySlots> getAvailableSlots(String dateStr, String physicianFullName) {
        LocalDate startDate;
        try {
            startDate = LocalDate.parse(dateStr, DATE_FMT);
        } catch (DateTimeParseException ex) {
            throw new InvalidAppointmentException("Formato de data inválido (use dd/MM/yyyy): " + dateStr);
        }

        LocalDate today = LocalDate.now();
        LocalDate maxAllowed = today.plusMonths(3);
        if (startDate.isAfter(maxAllowed)) {
            throw new InvalidAppointmentException(
                    String.format("Data fora do limite (só é permitido marcar até %s).", maxAllowed.format(DATE_FMT))
            );
        }

        AppointmentDateTimeValidator.validateDate(startDate);

        Physician physician = physicianRepository
                .findByBaseUserFullName(physicianFullName)
                .orElseThrow(() -> new InvalidAppointmentException(
                        "Physician não encontrado: " + physicianFullName));
        LocalTime workStart = physician.getWorkingHours().getStartTime();
        LocalTime workEnd   = physician.getWorkingHours().getEndTime();

        LocalTime lunchStart = LocalTime.of(13, 0);
        LocalTime lunchEnd   = LocalTime.of(14, 0);

        LocalTime nowTime = LocalTime.now();
        List<DailySlots> result = new ArrayList<>();

        for (int offset = 0; offset <= 10; offset++) {
            LocalDate currentDate = startDate.plusDays(offset);
            DayOfWeek dow = currentDate.getDayOfWeek();
            String dateFormatted = currentDate.format(DATE_FMT);
            String dayName = capitalizeFirstLetter(
                    dow.getDisplayName(TextStyle.FULL, new Locale("pt", "PT"))
            );

            if (dow == DayOfWeek.SUNDAY) {
                result.add(new DailySlots(dateFormatted, dayName, true, Collections.emptyList()));
                continue;
            }

            List<LocalTime> morningSlots = Collections.emptyList();
            if (workStart.isBefore(lunchStart)) {
                LocalTime morningEnd = workEnd.isBefore(lunchStart) ? workEnd : lunchStart;
                long count = Duration.between(workStart, morningEnd).toMinutes() / 25;
                morningSlots = Stream.iterate(workStart, t -> t.plusMinutes(25))
                        .limit(count)
                        .toList();
            }

            List<LocalTime> afternoonSlots = Collections.emptyList();
            if (workEnd.isAfter(lunchEnd)) {
                LocalTime afternoonStart = workStart.isAfter(lunchEnd) ? workStart : lunchEnd;
                long count = Duration.between(afternoonStart, workEnd).toMinutes() / 25;
                afternoonSlots = Stream.iterate(afternoonStart, t -> t.plusMinutes(25))
                        .limit(count)
                        .toList();
            }

            List<LocalTime> allSlots = Stream.concat(morningSlots.stream(), afternoonSlots.stream())
                    .toList();

            List<LocalTime> occupied = appointmentRepository
                    .findByPhysicianAndDate(physicianFullName, currentDate)
                    .stream()
                    .map(Appointment::getStartTime)
                    .toList();

            Stream<LocalTime> availableStream = allSlots.stream()
                    .filter(slot -> !occupied.contains(slot));

            if (currentDate.equals(today)) {
                availableStream = availableStream.filter(slot -> slot.isAfter(nowTime));
            }

            List<String> freeSlots = availableStream
                    .map(slot -> slot.format(TIME_FMT))
                    .toList();

            result.add(new DailySlots(dateFormatted, dayName, false, freeSlots));
        }

        return result;
    }

    public Appointment P_create(CreateAppointmentRequest request) {
        LocalDate date;
        try {
            date = LocalDate.parse(request.getDate(), DATE_FMT);
        } catch (DateTimeParseException ex) {
            throw new InvalidAppointmentException(
                    "Formato de data inválido (use dd-MM-yyyy): " + request.getDate()
            );
        }
        date = AppointmentDateTimeValidator.validateDate(date);

        LocalTime start;
        try {
            start = LocalTime.parse(request.getStartTime(), TIME_FMT);
        } catch (DateTimeParseException ex) {
            throw new InvalidAppointmentException(
                    "Formato de hora inválido (use HH:mm): " + request.getStartTime()
            );
        }
        LocalTime end = start.plusMinutes(20);
        validator.validateTimeSlot(date, start, end);

        Appointment ap = new Appointment();
        ap.setDate(date);
        ap.setStartTime(start);
        ap.setEndTime(end);
        ap.setConsultationType(ConsultationType.valueOf(request.getConsultationType()));
        ap.setDetails(request.getDetails());


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Patient patient = patientRepository
                .findByBaseUserUsername(username)
                .orElseThrow(() ->
                        new EntityNotFoundException("Paciente não encontrado para username: " + username)
                );

        String fullName = patient.getBaseUser().getFullName();
        ap.setPatientFullName(fullName);
        ap.setPatient(patient);


        ap.setPhysicianFullName(request.getPhysicianFullName());
        Physician physician = physicianRepository
                .findByBaseUserFullName(request.getPhysicianFullName())
                .orElseThrow(() ->
                        new EntityNotFoundException("Médico não encontrado: " + request.getPhysicianFullName())
                );
        ap.setPhysician(physician);
        ap.setStatus(AppointmentStatus.SCHEDULED);

        if (appointmentRepository.existsByPhysicianAndDateAndStartTime(physician, date, start)) {
            throw new InvalidAppointmentException(
                    "Conflito: o médico " + request.getPhysicianFullName() +" já possui consulta às " + request.getStartTime()
            );
        }
        return appointmentRepository.save(ap);
    }

    public List<PhysicianCount> getTop5Physicians(String startDateStr, String endDateStr) {
        LocalDate startDate;
        try {
            startDate = LocalDate.parse(startDateStr, DATE_FMT);
        } catch (DateTimeParseException ex) {
            throw new InvalidDateException("Formato de data inválido (use dd/MM/yyyy): " + startDateStr);
        }
        LocalDate endDate;
        try {
            endDate = LocalDate.parse(endDateStr, DATE_FMT);
        } catch (DateTimeParseException ex) {
            throw new InvalidDateException("Formato de data inválido (use dd/MM/yyyy): " + endDateStr);
        }
        if (endDate.isBefore(startDate)) {
            throw new InvalidDateException(
                    "Data de fim não pode ser anterior à data de início: " + endDateStr
            );
        }
        return appointmentRepository.findTop5PhysiciansByDateBetween(
                startDate,
                endDate,
                PageRequest.of(0, 5)
        );
    }
    @Transactional
    public List<Appointment> getAppointments(String physicianName) {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Patient patient = patientRepository
                .findByBaseUserUsername(username)
                .orElseThrow(() ->
                        new EntityNotFoundException("Patient" + username + " encontrado")
                );

        List<Appointment> appointments =
                appointmentRepository.findByPatientAndPhysicianName(patient, physicianName);

        if (appointments.isEmpty()) {
            throw new EntityNotFoundException("You don't have any appointments with physician " + physicianName);
        }
        return appointments;
    }



    @Transactional(readOnly = true)
    public List<AgeGroupStatsDto> getStatsByPatientAgeGroup() {

        List<Appointment> all = appointmentRepository.findAll();


        Map<String, List<Long>> grouped = all.stream()
                .filter(a -> a.getPatient() != null && a.getDate() != null)
                .collect(Collectors.groupingBy(
                        a -> {
                            int age = Period.between(
                                    a.getPatient().getDateOfBirth(),
                                    a.getDate()
                            ).getYears();
                            int bucket = (age / 10) * 10;
                            return "%d–%d".formatted(bucket, bucket + 9);
                        },
                        Collectors.mapping(a ->
                                        ChronoUnit.MINUTES.between(a.getStartTime(), a.getEndTime()),
                                Collectors.toList()
                        )
                ));


        return grouped.entrySet().stream()
                .map(e -> {
                    List<Long> durations = e.getValue();
                    long count = durations.size();
                    double avg = durations.stream()
                            .mapToLong(Long::longValue)
                            .average()
                            .orElse(0.0);
                    return new AgeGroupStatsDto(e.getKey(), count, avg);
                })
                .sorted(Comparator.comparingInt(d -> Integer.parseInt(d.ageGroup().split("–")[0])))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Appointment> getPatientHistory() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Patient patient = patientRepository
                .findByBaseUserUsername(username)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Paciente não encontrado para username: " + username
                        )
                );

        return appointmentRepository.findByPatient(patient);
    }

    public List<Appointment> getUpcomingAppointments() {
        LocalDate today = LocalDate.now();
        List<AppointmentStatus> activeStatuses = List.of(
                AppointmentStatus.SCHEDULED,
                AppointmentStatus.RESCHEDULED
        );
        return appointmentRepository.findByDateGreaterThanEqualAndStatusInOrderByDateAscStartTimeAsc(
                today, activeStatuses);
    }

    public void cancel(Long id) {
        Appointment ap = appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Consulta não encontrada: " + id));
        ap.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(ap);
    }

    public void reschedule(Long id, String newDate, String newStartTime) {
        Appointment ap = appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Consulta não encontrada: " + id));

        LocalDate date = LocalDate.parse(newDate, DATE_FMT);
        LocalTime start = LocalTime.parse(newStartTime, TIME_FMT);
        LocalTime end = start.plusMinutes(20);

        LocalDateTime newDateTime = LocalDateTime.of(date, start);
        if (newDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("A nova data e hora da consulta não podem ser anteriores ao momento atual.");
        }

        validator.validateTimeSlot(date, start, end);

        Physician physician = ap.getPhysician();
        List<Appointment> physicianAppointments = appointmentRepository
                .findByPhysicianIdAndDate(physician.getId(), date);

        for (Appointment other : physicianAppointments) {
            if (!other.getId().equals(ap.getId())) {
                LocalTime otherStart = other.getStartTime();
                LocalTime otherEnd = other.getEndTime();

                boolean overlaps = !start.isAfter(otherEnd) && !end.isBefore(otherStart);
                if (overlaps) {
                    throw new IllegalArgumentException("O médico já tem uma consulta nesse horário.");
                }
            }
        }

        ap.setDate(date);
        ap.setStartTime(start);
        ap.setEndTime(end);
        ap.setStatus(AppointmentStatus.RESCHEDULED);

        appointmentRepository.save(ap);

    }
    public Map<String, Double> getAverageDurationPerPhysician() {
        List<Object[]> results = appointmentRepository.findAppointmentTimesPerPhysician();
        Map<String, List<Long>> durationsByPhysician = new HashMap<>();

        for (Object[] row : results) {
            String physicianName = (String) row[0];
            LocalTime start = (LocalTime) row[1];
            LocalTime end = (LocalTime) row[2];

            long duration = Duration.between(start, end).toMinutes();

            durationsByPhysician.computeIfAbsent(physicianName, k -> new ArrayList<>()).add(duration);
        }

        Map<String, Double> averages = new HashMap<>();
        for (Map.Entry<String, List<Long>> entry : durationsByPhysician.entrySet()) {
            List<Long> durations = entry.getValue();
            double avg = durations.stream().mapToLong(Long::longValue).average().orElse(0);
            averages.put(entry.getKey(), avg);
        }

        return averages;
    }
    @Scheduled(fixedRate =  1200000)
    public void updatePastAppointmentsToCompleted() {
        List<Appointment> appointments = appointmentRepository.findByStatusIn(
                List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.RESCHEDULED)
        );
        LocalDateTime now = LocalDateTime.now();

        for (Appointment appointment : appointments) {
            LocalDateTime appointmentDateTime = LocalDateTime.of(
                    appointment.getDate(),
                    appointment.getStartTime()
            );
            if (appointmentDateTime.isBefore(now)) {
                appointment.setStatus(AppointmentStatus.COMPLETED);
                appointmentRepository.save(appointment);
            }
        }
    }

}


