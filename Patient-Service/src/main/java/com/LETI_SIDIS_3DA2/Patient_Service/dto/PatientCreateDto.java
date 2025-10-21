package com.LETI_SIDIS_3DA2.Patient_Service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;


public class PatientCreateDto {



    @NotBlank
    public String fullName;

    @Email @NotBlank
    public String email;

    @NotNull
    public LocalDate birthDateLocal;

    @NotBlank
    public String phoneNumber;

    public String insurancePolicyNumber;
    public String insuranceCompany;

    @NotNull
    public LocalDate consentDate;

    public String photoUrl;
    public List<String> healthConcerns;

}
