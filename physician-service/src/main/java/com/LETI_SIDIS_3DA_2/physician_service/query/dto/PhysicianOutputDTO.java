package com.LETI_SIDIS_3DA_2.physician_service.query.dto;

import org.springframework.hateoas.RepresentationModel;

import java.time.LocalTime;

public class PhysicianOutputDTO extends RepresentationModel<PhysicianOutputDTO> {
    private Long id;
    private String fullName;
    private String specialtyName; // Nome da especialidade
    private String departmentAcronym; // apenas pega na sigla
    private String email;
    private String phoneNumber;
    private String address;
    private LocalTime workStartTime;
    private LocalTime workEndTime;
    private String optionalDescription;
    private String profilePhotoUrl;

    public PhysicianOutputDTO(Long id, String fullName, String specialtyName, String departmentAcronym,
                              String email, String phoneNumber, String address,
                              LocalTime workStartTime, LocalTime workEndTime, String optionalDescription, String profilePhotoUrl) {
        this.id = id;
        this.fullName = fullName;
        this.specialtyName = specialtyName;
        this.departmentAcronym = departmentAcronym;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.workStartTime = workStartTime;
        this.workEndTime = workEndTime;
        this.optionalDescription = optionalDescription;
        this.profilePhotoUrl = profilePhotoUrl;
    }

    // Getters
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getSpecialtyName() { return specialtyName; }
    public String getDepartmentAcronym() { return departmentAcronym; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public LocalTime getWorkStartTime() { return workStartTime; }
    public LocalTime getWorkEndTime() { return workEndTime; }
    public String getOptionalDescription() { return optionalDescription; }
    public String getProfilePhotoUrl() { return profilePhotoUrl; }

    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setSpecialtyName(String specialtyName) { this.specialtyName = specialtyName; }
}
