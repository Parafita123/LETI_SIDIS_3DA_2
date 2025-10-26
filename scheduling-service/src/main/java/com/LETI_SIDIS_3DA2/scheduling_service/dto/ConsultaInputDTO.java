package com.LETI_SIDIS_3DA2.scheduling_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ConsultaInputDTO {
    @NotNull(message = "O ID do médico é obrigatório.")
    private Long physicianId;

    @NotNull(message = "A data e hora da consulta são obrigatórias.")
    @Future(message = "A data da consulta deve ser no futuro.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateTime;

    @NotBlank(message = "O tipo de consulta não pode estar em branco.")
    @Size(max = 50, message = "O tipo de consulta não pode exceder 50 caracteres.")
    private String consultationType;

    @Size(max = 255, message = "As notas não podem exceder 255 caracteres.")
    private String notes;

    // Getters e Setters

    public Long getPhysicianId() { return physicianId; }
    public void setPhysicianId(Long physicianId) { this.physicianId = physicianId; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getConsultationType() { return consultationType; }
    public void setConsultationType(String consultationType) { this.consultationType = consultationType; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}