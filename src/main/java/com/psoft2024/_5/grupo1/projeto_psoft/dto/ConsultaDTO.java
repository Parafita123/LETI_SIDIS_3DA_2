package com.psoft2024._5.grupo1.projeto_psoft.dto;

import java.time.LocalDateTime;

public class ConsultaDTO {


    public Long patientId;
    public Long physicianId;
    public LocalDateTime dateTime;
    public String consultationType;
    public String notes;


    public ConsultaDTO() {}

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getPhysicianId() { return physicianId; }
    public void setPhysicianId(Long physicianId) { this.physicianId = physicianId; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getConsultationType() { return consultationType; }
    public void setConsultationType(String consultationType) { this.consultationType = consultationType; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

}
