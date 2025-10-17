package com.LETI_SIDIS_3DA_2.physician_service.dto;

public class PhysicianAppointmentCountDTO {
    private Long physicianId;
    private String physicianFullName;
    private Long appointmentCount;

    // Construtor para ser usado na query JPQL
    public PhysicianAppointmentCountDTO(Long physicianId, String physicianFullName, Long appointmentCount) {
        this.physicianId = physicianId;
        this.physicianFullName = physicianFullName;
        this.appointmentCount = appointmentCount;
    }

    // Getters
    public Long getPhysicianId() { return physicianId; }
    public String getPhysicianFullName() { return physicianFullName; }
    public Long getAppointmentCount() { return appointmentCount; }
}

