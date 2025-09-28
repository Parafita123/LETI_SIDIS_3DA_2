package com.psoft.clinic.appointmentsmanagement.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.psoft.clinic.appointmentsmanagement.model.AppointmentStatus;


import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentView {
    private String patientFullName;
    private String physicianFullName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String consultationType;
}