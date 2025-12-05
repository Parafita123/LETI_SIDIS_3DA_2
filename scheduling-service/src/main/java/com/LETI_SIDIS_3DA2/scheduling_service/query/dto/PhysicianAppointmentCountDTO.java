package com.LETI_SIDIS_3DA2.scheduling_service.query.dto;

public class PhysicianAppointmentCountDTO {
    private Long physicianId;
    private String physicianFullName;
    private Long appointmentCount;

    public PhysicianAppointmentCountDTO(Long physicianId, String physicianFullName, Long appointmentCount) {
        this.physicianId = physicianId;
        this.physicianFullName = physicianFullName;
        this.appointmentCount = appointmentCount;
    }

    // Getters e Setters
    public Long getPhysicianId() { return physicianId; }
    public void setPhysicianId(Long physicianId) { this.physicianId = physicianId; }

    public String getPhysicianFullName() { return physicianFullName; }
    public void setPhysicianFullName(String physicianFullName) { this.physicianFullName = physicianFullName; }

    public Long getAppointmentCount() { return appointmentCount; }
    public void setAppointmentCount(Long appointmentCount) { this.appointmentCount = appointmentCount; }
}