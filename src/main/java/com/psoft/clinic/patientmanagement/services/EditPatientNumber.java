package com.psoft.clinic.patientmanagement.services;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
public class EditPatientNumber {

    @NotBlank(message = "Número de telefone é obrigatório")
    @Pattern(
            regexp = "\\+?[0-9\\- ]{7,20}",
            message = "Formato de número inválido"
    )
    @Getter @Setter
    private String phoneNumber;

}
