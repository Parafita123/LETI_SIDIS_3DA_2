package com.psoft.clinic.appointmentsmanagement.services;

import com.psoft.clinic.appointmentsmanagement.model.AppointmentStatus;
import com.psoft.clinic.appointmentsmanagement.repository.SpringBootAppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    @Autowired
    private SpringBootAppointmentRepository appointmentRepository;

    public MonthlyReportResponse generateMonthlyReport(int month, int year) {
        // Contar total de consultas
        long totalAppointments = appointmentRepository.countByYearAndMonth(year, month);

        // Contar por status
        long scheduledAppointments = appointmentRepository.countByYearAndMonthAndStatus(year, month, AppointmentStatus.SCHEDULED);
        long completedAppointments = appointmentRepository.countByYearAndMonthAndStatus(year, month, AppointmentStatus.COMPLETED);
        long cancelledAppointments = appointmentRepository.countByYearAndMonthAndStatus(year, month, AppointmentStatus.CANCELLED);
        long rescheduledAppointments = appointmentRepository.countByYearAndMonthAndStatus(year, month, AppointmentStatus.RESCHEDULED);

        return new MonthlyReportResponse(
                month,
                year,
                totalAppointments,
                scheduledAppointments,
                completedAppointments,
                cancelledAppointments,
                rescheduledAppointments
        );
    }
}