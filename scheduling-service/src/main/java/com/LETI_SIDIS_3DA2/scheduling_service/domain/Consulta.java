package com.LETI_SIDIS_3DA2.scheduling_service.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultas")
public class Consulta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long patientId;

    @Column(nullable = false)
    private Long physicianId;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false)
    private Integer duration;

    @Column(nullable = false, length = 50)
    private String consultationType;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 255)
    private String notes;

    // --- CORREÇÕES ABAIXO ---

    // 1. Adicionar construtor protegido sem argumentos para o JPA
    protected Consulta() {}

    // 2. O teu construtor público (já estava bom)
    public Consulta(Long patientId, Long physicianId, LocalDateTime dateTime, Integer duration, String consultationType, String status, String notes) {
        this.patientId = patientId;
        this.physicianId = physicianId;
        this.dateTime = dateTime;
        this.duration = duration;
        this.consultationType = consultationType;
        this.status = status;
        this.notes = notes;
    }

    // 3. Adicionar Getters e Setters para todos os campos

    public Long getId() {
        return id;
    }

    // Não precisamos de um setId, pois é gerado pela BD.

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getPhysicianId() {
        return physicianId;
    }

    public void setPhysicianId(Long physicianId) {
        this.physicianId = physicianId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getConsultationType() {
        return consultationType;
    }

    public void setConsultationType(String consultationType) {
        this.consultationType = consultationType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}