package com.LETI_SIDIS_3DA2.Patient_Service.messaging;

import java.time.LocalDate;
import java.util.List;

public class PatientEventPayload {

    private Long id;
    private String fullName;
    private String email;
    private LocalDate birthDateLocal;
    private String phoneNumber;
    private String insuranceCompany;
    private String insurancePolicyNumber;
    private String photoUrl;
    private List<String> healthConcerns;

    public PatientEventPayload(Long id, String fullName, String email,
                               LocalDate birthDateLocal, String phoneNumber,
                               String insuranceCompany, String insurancePolicyNumber,
                               String photoUrl, List<String> healthConcerns) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.birthDateLocal = birthDateLocal;
        this.phoneNumber = phoneNumber;
        this.insuranceCompany = insuranceCompany;
        this.insurancePolicyNumber = insurancePolicyNumber;
        this.photoUrl = photoUrl;
        this.healthConcerns = healthConcerns;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public LocalDate getBirthDateLocal() { return birthDateLocal; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getInsuranceCompany() { return insuranceCompany; }
    public String getInsurancePolicyNumber() { return insurancePolicyNumber; }
    public String getPhotoUrl() { return photoUrl; }
    public List<String> getHealthConcerns() { return healthConcerns; }
}
