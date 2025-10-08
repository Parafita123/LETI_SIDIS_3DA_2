package com.psoft2024._5.grupo1.projeto_psoft.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultas")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(optional = false)
    @JoinColumn(name = "physician_id")
    private Physician physician;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false)
    private Integer duration; // duração da consulta em minutos

    @Column(nullable = false, length = 50)
    private String consultationType;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 255)
    private String notes;

    protected Consulta() {}

    public Consulta(Patient paciente, Physician medico, LocalDateTime dateTime,
                    Integer duration, String consultationType, String status, String notes) {
        this.patient = paciente;
        this.physician = medico;
        this.dateTime = dateTime;
        this.duration = duration;
        this.consultationType = consultationType;
        this.status = status;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public Patient getPatient() { return patient; }
    public Physician getPhysician() { return physician; }
    public LocalDateTime getDateTime() { return dateTime; }
    public Integer getDuration() { return duration; }
    public String getConsultationType() { return consultationType; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public void setPhysician(Physician physician) {
        this.physician = physician;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public void setConsultationType(String consultationType) {
        this.consultationType = consultationType;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

}
