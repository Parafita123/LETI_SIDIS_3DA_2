package com.LETI_SIDIS_3DA_2.physician_service.messaging;


import java.time.LocalTime;

public class PhysicianEventPayload {

    private Long id;
    private String fullName;
    private String specialtyName;
    private String departmentAcronym;
    private String email;
    private String phoneNumber;
    private LocalTime workStartTime;
    private LocalTime workEndTime;

    // Construtor vazio para o Jackson
    public PhysicianEventPayload() { }

    public PhysicianEventPayload(Long id,
                                 String fullName,
                                 String specialtyName,
                                 String departmentAcronym,
                                 String email,
                                 String phoneNumber,
                                 LocalTime workStartTime,
                                 LocalTime workEndTime) {
        this.id = id;
        this.fullName = fullName;
        this.specialtyName = specialtyName;
        this.departmentAcronym = departmentAcronym;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.workStartTime = workStartTime;
        this.workEndTime = workEndTime;
    }

    // Factory a partir do agregado Physician
    public static PhysicianEventPayload fromDomain(
            com.LETI_SIDIS_3DA_2.physician_service.domain.Physician physician) {

        return new PhysicianEventPayload(
                physician.getId(),
                physician.getFullName(),
                physician.getSpecialty() != null ? physician.getSpecialty().getName() : null,
                physician.getDepartment() != null ? physician.getDepartment().getAcronym() : null,
                physician.getEmail(),
                physician.getPhoneNumber(),
                physician.getWorkStartTime(),
                physician.getWorkEndTime()
        );
    }

    // getters e setters (necessários para o Jackson)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialtyName() {
        return specialtyName;
    }

    public void setSpecialtyName(String specialtyName) {
        this.specialtyName = specialtyName;
    }

    public String getDepartmentAcronym() {
        return departmentAcronym;
    }

    public void setDepartmentAcronym(String departmentAcronym) {
        this.departmentAcronym = departmentAcronym;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalTime getWorkStartTime() {
        return workStartTime;
    }

    public void setWorkStartTime(LocalTime workStartTime) {
        this.workStartTime = workStartTime;
    }

    public LocalTime getWorkEndTime() {
        return workEndTime;
    }

    public void setWorkEndTime(LocalTime workEndTime) {
        this.workEndTime = workEndTime;
    }
}