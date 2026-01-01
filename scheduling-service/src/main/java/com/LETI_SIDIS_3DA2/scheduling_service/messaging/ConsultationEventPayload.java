package com.LETI_SIDIS_3DA2.scheduling_service.messaging;

import java.time.LocalDateTime;

public class ConsultationEventPayload {

    private Long id;
    private Long patientId;
    private Long physicianId;
    private LocalDateTime dateTime;
    private String status;
    private String consultationType;

    public ConsultationEventPayload() { }

    public ConsultationEventPayload(Long id, Long patientId, Long physicianId,
                                    LocalDateTime dateTime, String status,
                                    String consultationType) {
        this.id = id;
        this.patientId = patientId;
        this.physicianId = physicianId;
        this.dateTime = dateTime;
        this.status = status;
        this.consultationType = consultationType;
    }

    // getters e setters
    public Long getId() {return id;}
    public void setId(Long id) { this.id = id; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getPhysicianId() { return physicianId; }
    public void setPhysicianId(Long physicianId) { this.physicianId = physicianId;}
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getConsultationType() { return consultationType; }
    public void setConsultationType(String consultationType) { this.consultationType = consultationType; }

}


