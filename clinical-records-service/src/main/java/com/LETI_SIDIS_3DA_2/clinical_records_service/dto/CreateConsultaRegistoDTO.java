package com.LETI_SIDIS_3DA_2.clinical_records_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateConsultaRegistoDTO {
    @NotNull(message = "O ID da consulta é obrigatório.")
    private Long consultaId;

    @NotBlank(message = "O diagnóstico não pode estar em branco.")
    @Size(max = 500)
    private String diagnosis;

    @Size(max = 1000)
    private String treatmentRecommendations;

    @Size(max = 1000)
    private String prescriptions;

    // --- ADICIONA OS GETTERS E SETTERS ABAIXO ---

    public Long getConsultaId() {
        return consultaId;
    }

    public void setConsultaId(Long consultaId) {
        this.consultaId = consultaId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatmentRecommendations() {
        return treatmentRecommendations;
    }

    public void setTreatmentRecommendations(String treatmentRecommendations) {
        this.treatmentRecommendations = treatmentRecommendations;
    }

    public String getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(String prescriptions) {
        this.prescriptions = prescriptions;
    }
}