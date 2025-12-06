package com.LETI_SIDIS_3DA2.Patient_Service.query.dto;

import java.time.LocalDate;
import java.util.List;


public class PatientDto {
    public Long id;
    public String fullName;
    public String email;
    public LocalDate birthDateLocal;
    public String phoneNumber;
    public String insurancePolicyNumber;
    public String insuranceCompany;
    public LocalDate consentDate;
    public Long getId() {
        return id;
    }
    public String photoUrl;
    public List<String> healthConcerns;
    public String getPhotoUrl() { return photoUrl; }
    public List<String> getHealthConcerns() { return healthConcerns; }
}
