package com.psoft.clinic.appointmentsmanagement.services;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportResponse {
    private int month;
    private int year;
    private long totalAppointments;
    private long scheduledAppointments;
    private long completedAppointments;
    private long cancelledAppointments;
    private long rescheduledAppointments;

}
