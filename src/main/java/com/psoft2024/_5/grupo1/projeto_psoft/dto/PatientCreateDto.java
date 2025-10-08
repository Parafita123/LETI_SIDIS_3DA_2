package com.psoft2024._5.grupo1.projeto_psoft.dto;

import jakarta.validation.constraints.*;
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
