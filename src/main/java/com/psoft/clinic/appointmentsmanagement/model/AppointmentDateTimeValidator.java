package com.psoft.clinic.appointmentsmanagement.model;

import com.psoft.clinic.exceptions.InvalidAppointmentException;
import org.springframework.stereotype.Component;

import java.time.*;

@Component
public class AppointmentDateTimeValidator {

    public static LocalDate validateDate(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new InvalidAppointmentException("Data deve ser hoje ou futura.");
        }
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new InvalidAppointmentException("Domingo a clínica está fechada.");
        }
        return date;
    }

    public void validateTimeSlot(LocalDate date, LocalTime start, LocalTime end) {
        DayOfWeek dow = date.getDayOfWeek();

        LocalTime morningOpen    = LocalTime.of(9, 0);
        LocalTime morningClose   = LocalTime.of(13, 0);
        LocalTime afternoonOpen  = LocalTime.of(14, 0);
        LocalTime afternoonClose = LocalTime.of(20, 0);

        boolean inAllowedWindow;
        if (dow == DayOfWeek.SATURDAY) {
            inAllowedWindow = !start.isBefore(morningOpen) && !end.isAfter(morningClose);
        } else {
            boolean inMorning   = !start.isBefore(morningOpen)   && !end.isAfter(morningClose);
            boolean inAfternoon = !start.isBefore(afternoonOpen) && !end.isAfter(afternoonClose);
            inAllowedWindow = inMorning || inAfternoon;
        }
        if (!inAllowedWindow) {
            throw new InvalidAppointmentException(
                    (dow == DayOfWeek.SATURDAY
                            ? "Sábado só 09:00–13:00."
                            : "Seg–Sex 09:00–13:00 e 14:00–20:00.")
                            + " Horário solicitado fora do expediente."
            );
        }

        // Verifica duração exata de 20 minutos
        Duration duration = Duration.between(start, end);
        if (!duration.equals(Duration.ofMinutes(20))) {
            throw new InvalidAppointmentException(
                    "Cada agendamento deve durar exatamente 20 minutos."
            );
        }

        // Verifica que o início está num múltiplo de 25 minutos (0, 25, 50)
        int minute = start.getMinute();
        if (minute % 25 != 0) {
            throw new InvalidAppointmentException(
                    "Início deve ser múltiplo de 25 minutos (ex: 09:00, 09:25, 09:50)."
            );
        }
    }
}
