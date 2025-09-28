package com.psoft.clinic.appointmentsmanagement.services;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentRequest {
    @NotBlank(message = "Data é obrigatória")
    private String date;

    @NotBlank(message = "Horário de início é obrigatório")
    private String startTime;

    @NotBlank(message = "Tipo de consulta é obrigatório")
    private String consultationType;

    @NotBlank(message = "Nome completo do paciente é obrigatório")
    private String patientFullName;

    @NotBlank(message = "Nome completo do médico é obrigatório")
    private String physicianFullName;


    private String details;
}
