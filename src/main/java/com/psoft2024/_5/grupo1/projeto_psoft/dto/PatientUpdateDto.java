// src/main/java/com/psoft2024/_5/grupo1/projeto_psoft/dto/PatientUpdateDto.java
package com.psoft2024._5.grupo1.projeto_psoft.dto;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

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
