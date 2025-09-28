package com.psoft.clinic.appointmentsmanagement.services;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleRequest {
    @NotBlank(message = "Nova data é obrigatória")
    private String newDate;

    @NotBlank(message = "Novo horário é obrigatório")
    private String newStartTime;
}

