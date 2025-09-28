package com.psoft.clinic.physiciansmanagement.services;

import com.psoft.clinic.model.PhoneType;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Data
@NoArgsConstructor
public class CreatePhysicianRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 6)
    private String password;

    @NotBlank
    @Email(message = "Email inválido")
    @Pattern(
            regexp = "[^@\\s]+@[^@\\s]+\\.[A-Za-z]{3}",
            message = "Email deve estar no formato xxx@xxx.xxx"
    )
    private String email;

    @NotBlank
    private String street;

    @NotBlank
    private String city;

    @NotBlank
    private String district;

    @NotBlank
    @Pattern(
            regexp = "\\d{4}-\\d{3}",
            message = "Zip deve estar no formato 0000-000"
    )
    private String zip;

    @NotBlank
    private String country;

    @NotBlank
    @Pattern(
            regexp = "\\d{9,11}",
            message = "O número de telefone deve conter entre 7 e 10 dígitos numéricos"
    )
    private String phoneNumber;

    private PhoneType phoneType;

    @NotBlank
    private String departmentSigla;

    @NotBlank
    private String speciality;

    @NotNull
    @Pattern(regexp = "([01]\\d|2[0-3]):[0-5]\\d", message = "startTime deve estar no formato HH:mm")
    private String startTime;

    @NotNull
    @Pattern(regexp = "([01]\\d|2[0-3]):[0-5]\\d", message = "endTime deve estar no formato HH:mm")
    private String endTime;

    @AssertTrue(message = "startTime não deve ser antes das 09:00")
    private boolean isStartTimeValid() {
        try {
            return !LocalTime.parse(startTime).isBefore(LocalTime.of(9, 0));
        } catch (DateTimeParseException | NullPointerException e) {
            return true;
        }
    }

    @AssertTrue(message = "endTime não deve ser superior às 20:00")
    private boolean isEndTimeValid() {
        try {
            return !LocalTime.parse(endTime).isAfter(LocalTime.of(20, 0));
        } catch (DateTimeParseException | NullPointerException e) {
            return true;
        }
    }
}
