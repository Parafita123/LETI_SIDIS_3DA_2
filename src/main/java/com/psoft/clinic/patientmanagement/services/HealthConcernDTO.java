package com.psoft.clinic.patientmanagement.services;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Data
@NoArgsConstructor
public class HealthConcernDTO {




    private String description;


    @NotNull(message = "dateNoticed é obrigatório")
    @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$",
            message = "dateNoticed deve estar em dd/MM/yyyy")
    private String dateNoticed;

    @NotBlank(message = "treatment é obrigatório")
    private String treatment;


    @NotNull(message = "persisting é obrigatório")
    private Boolean persisting;

    @Pattern(regexp = "^$|^\\d{2}/\\d{2}/\\d{4}$",
            message = "dateResolved, se fornecido, deve estar em dd/MM/yyyy")
    private String dateResolved;

    @AssertTrue(message = "Quando persisting for false, dateResolved (dd/MM/yyyy) é obrigatório; " +
            "quando persisting for true, dateResolved não pode ser fornecido")
    public boolean isDateResolvedConsistent() {
        if (persisting == null) {

            return true;
        }

        boolean hasResolved = dateResolved != null && !dateResolved.isBlank();
        if (persisting) {

            return !hasResolved;
        } else {

            if (!hasResolved) {
                return false;
            }
            try {
                LocalDate.parse(dateResolved, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                return true;
            } catch (DateTimeParseException e) {
                return false;
            }
        }
    }


}
