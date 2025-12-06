// src/main/java/com/psoft2024/_5/grupo1/projeto_psoft/dto/PatientUpdateDto.java
package com.LETI_SIDIS_3DA2.Patient_Service.command.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

public class PatientUpdateDto {
    @Email @NotBlank
    public String email;
    @NotBlank
    public String phoneNumber;
    public String insuranceCompany;
    public String insurancePolicyNumber;
    public String photoUrl;
    public LocalDate consentDate;
    public List<String> healthConcerns;
}
