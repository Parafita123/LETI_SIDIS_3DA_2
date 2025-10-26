package com.LETI_SIDIS_3DA2.scheduling_service.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
// import org.springframework.hateoas.RepresentationModel; // Para HATEOAS no futuro

import java.time.LocalDateTime;

// Se usares HATEOAS: public class ConsultaOutPutDTO extends RepresentationModel<ConsultaOutPutDTO> {
public class ConsultaOutPutDTO {

    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateTime;

    private Integer duration;
    private String consultationType;
    private String status;
    private String notes;

    // Informação agregada de outros serviços
    private Long physicianId;
    private String physicianName; // Será obtido do Physician Service

    private Long patientId;
    private String patientName;   // Será obtido do Patient Service

    // Construtor vazio (útil para mapeadores)
    public ConsultaOutPutDTO() {}

    // Getters e Setters para todos os campos

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getConsultationType() { return consultationType; }
    public void setConsultationType(String consultationType) { this.consultationType = consultationType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Long getPhysicianId() { return physicianId; }
    public void setPhysicianId(Long physicianId) { this.physicianId = physicianId; }

    public String getPhysicianName() { return physicianName; }
    public void setPhysicianName(String physicianName) { this.physicianName = physicianName; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
}