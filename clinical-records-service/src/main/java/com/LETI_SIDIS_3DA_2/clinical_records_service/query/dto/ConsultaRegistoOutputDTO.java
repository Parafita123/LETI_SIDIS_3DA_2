package com.LETI_SIDIS_3DA_2.clinical_records_service.query.dto;

import java.time.LocalDateTime;

public class ConsultaRegistoOutputDTO {
    private Long id;
    private Long consultaId;
    private String diagnosis;
    private String treatmentRecommendations;
    private String prescriptions;
    private LocalDateTime createdAt;

    // --- ADICIONA OS GETTERS E SETTERS ABAIXO ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}