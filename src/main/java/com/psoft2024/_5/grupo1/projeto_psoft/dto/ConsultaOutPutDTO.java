package com.psoft2024._5.grupo1.projeto_psoft.dto;

import java.time.LocalDateTime;


public class ConsultaOutPutDTO {

    private Long id;
    private String patientName;
    private String physicianName;
    private LocalDateTime dateTime;
    private Integer duration;
    private String consultationType;
    private String status;
    private String notes;

    public ConsultaOutPutDTO() {}

    public ConsultaOutPutDTO(Long id, String patientName, String physicianName,
                             LocalDateTime dateTime, Integer duration,
                             String consultationType, String status, String notes) {
        this.id = id;
        this.patientName = patientName;
        this.physicianName = physicianName;
        this.dateTime = dateTime;
        this.duration = duration;
        this.consultationType = consultationType;
        this.status = status;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPhysicianName() {
        return physicianName;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public String getConsultationType() {
        return consultationType;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

}
