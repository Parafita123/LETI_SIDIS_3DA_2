package com.psoft.clinic.patientmanagement.services;

import com.psoft.clinic.model.Gender;
import com.psoft.clinic.model.PhoneType;
import com.psoft.clinic.patientmanagement.services.HealthConcernDTO;
import jakarta.annotation.Nullable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CreatePatientRequest {

    @NotBlank(message = "Username é obrigatório")
    private String username;

    @NotBlank(message = "Password é obrigatória")
    @Size(min = 6, message = "Password deve ter ao menos 6 caracteres")
    private String password;

    @NotBlank(message = "Full name é obrigatório")
    private String fullName;

    @Pattern(regexp = "\\d{9}", message = "NSNS deve conter 9 dígitos numéricos")
    private String nsns;

    @NotNull(message = "Gender é obrigatório")
    private String gender;

    // --- Contact ---
    @Email(message = "Email inválido")
    @NotBlank(message = "Email é obrigatório")
    private String email;

    // --- Address (planos no JSON) ---
    @NotBlank(message = "Street é obrigatória")
    private String street;

    @NotBlank(message = "City é obrigatória")
    private String city;

    @NotBlank(message = "District é obrigatório")
    private String district;

    @NotBlank(message = "Zip é obrigatório")
    private String zip;

    @NotBlank(message = "Country é obrigatório")
    private String country;

    @NotBlank(message = "Phone number é obrigatório")
    @Pattern(
            regexp = "\\+?[0-9\\- ]{7,20}",
            message = "Número de telefone inválido"
    )
    private String phoneNumber;

    private PhoneType phoneType;

    @NotBlank(message = "Date of birth é obrigatória")
    @Pattern(
            regexp = "^(0[1-9]|[12]\\d|3[01])/(0[1-9]|1[0-2])/[0-9]{4}$",
            message = "Formato de data deve ser dd/MM/yyyy"
    )
    private String dateOfBirth;

    @Nullable
    private String insuranceProvider;

    @Nullable
    private String policyNumber;

    @Nullable
    private String consent;

    @Valid
    private List<HealthConcernDTO> healthConcerns;
}
