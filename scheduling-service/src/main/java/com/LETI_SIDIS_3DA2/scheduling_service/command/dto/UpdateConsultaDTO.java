package com.LETI_SIDIS_3DA2.scheduling_service.command.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class UpdateConsultaDTO {
    @NotNull(message = "A nova data e hora da consulta são obrigatórias.")
    @Future(message = "A data da consulta deve ser no futuro.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateTime;

    @Size(max = 255, message = "As notas não podem exceder 255 caracteres.")
    private String notes;

    // Getters e Setters

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}