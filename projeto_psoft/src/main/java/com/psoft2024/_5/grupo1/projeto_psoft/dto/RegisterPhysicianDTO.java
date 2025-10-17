package com.psoft2024._5.grupo1.projeto_psoft.dto;

public class RegisterPhysicianDTO {

    // @NotBlank
    private String fullName;

    // @NotNull
    private Long specialtyId;

    // @NotNull
    private Long departmentId;

    // @NotBlank
    // @Email
    private String email;

    // @NotBlank
    private String phoneNumber;

    // @NotBlank
    private String address;

    // @NotNull
    // Formato esperado: "HH:mm", ex: "09:00"
    private String workStartTime;

    // @NotNull
    private String workEndTime;

    private String optionalDescription;

    // Getters e Setters (ou Lombok @Data)
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Long getSpecialtyId() { return specialtyId; }
    public void setSpecialtyId(Long specialtyId) { this.specialtyId = specialtyId; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getWorkStartTime() { return workStartTime; }
    public void setWorkStartTime(String workStartTime) { this.workStartTime = workStartTime; }
    public String getWorkEndTime() { return workEndTime; }
    public void setWorkEndTime(String workEndTime) { this.workEndTime = workEndTime; }
    public String getOptionalDescription() { return optionalDescription; }
    public void setOptionalDescription(String optionalDescription) { this.optionalDescription = optionalDescription; }
}