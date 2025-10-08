package com.psoft2024._5.grupo1.projeto_psoft.dto;

import java.time.LocalDateTime;

public class ConsultasRegistoOutPutDTO {

    private Long id;
    private Long consultaId;
    private String diagnosis;
    private String treatmentRecommendations;
    private String prescriptions;
    private LocalDateTime createdAt;

    public ConsultasRegistoOutPutDTO() {}

    public ConsultasRegistoOutPutDTO(Long id, Long consultaId, String diagnosis,
                                    String treatmentRecommendations, String prescriptions,
                                    LocalDateTime createdAt) {
        this.id = id;
        this.consultaId = consultaId;
        this.diagnosis = diagnosis;
        this.treatmentRecommendations = treatmentRecommendations;
        this.prescriptions = prescriptions;
        this.createdAt = createdAt;
    }

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
