package com.LETI_SIDIS_3DA_2.clinical_records_service.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ConsultaRegistos")
public class ConsultaRegisto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // record number

    // REMOVE a relação direta
    // @ManyToOne(optional = false)
    // @JoinColumn(name = "consulta_id")
    // private Consulta consulta;

    // ADICIONA o ID da consulta
    @Column(nullable = false, name = "consulta_id")
    private Long consultaId;

    @Column(nullable = false, length = 500)
    private String diagnosis;

    @Column(length = 1000)
    private String treatmentRecommendations;

    @Column(length = 1000)
    private String prescriptions;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected ConsultaRegisto() {}

    // Construtor atualizado
    public ConsultaRegisto(Long consultaId, String diagnosis,
                           String treatmentRecommendations, String prescriptions,
                           LocalDateTime createdAt) {
        this.consultaId = consultaId;
        this.diagnosis = diagnosis;
        this.treatmentRecommendations = treatmentRecommendations;
        this.prescriptions = prescriptions;
        this.createdAt = createdAt;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public Long getConsultaId() { return consultaId; }
    public String getDiagnosis() { return diagnosis; }
    public String getTreatmentRecommendations() { return treatmentRecommendations; }
    public String getPrescriptions() { return prescriptions; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setConsultaId(Long consultaId) { this.consultaId = consultaId; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public void setTreatmentRecommendations(String treatmentRecommendations) { this.treatmentRecommendations = treatmentRecommendations; }
    public void setPrescriptions(String prescriptions) { this.prescriptions = prescriptions; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}