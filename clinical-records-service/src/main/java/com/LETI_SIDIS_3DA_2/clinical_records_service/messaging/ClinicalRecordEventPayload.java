package com.LETI_SIDIS_3DA_2.clinical_records_service.messaging;

import java.time.LocalDateTime;

public class ClinicalRecordEventPayload {

    private Long recordId;
    private Long consultaId;
    private String diagnosis;
    private String treatmentRecommendations;
    private String prescriptions;
    private LocalDateTime createdAt;

    public ClinicalRecordEventPayload() { }

    public ClinicalRecordEventPayload(Long recordId,
                                      Long consultaId,
                                      String diagnosis,
                                      String treatmentRecommendations,
                                      String prescriptions,
                                      LocalDateTime createdAt) {
        this.recordId = recordId;
        this.consultaId = consultaId;
        this.diagnosis = diagnosis;
        this.treatmentRecommendations = treatmentRecommendations;
        this.prescriptions = prescriptions;
        this.createdAt = createdAt;
    }

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }

    public Long getConsultaId() { return consultaId; }
    public void setConsultaId(Long consultaId) { this.consultaId = consultaId; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getTreatmentRecommendations() { return treatmentRecommendations; }
    public void setTreatmentRecommendations(String treatmentRecommendations) {
        this.treatmentRecommendations = treatmentRecommendations;
    }

    public String getPrescriptions() { return prescriptions; }
    public void setPrescriptions(String prescriptions) { this.prescriptions = prescriptions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
