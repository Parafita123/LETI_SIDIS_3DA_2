package com.psoft.clinic.patientmanagement.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Embeddable
@Data
@NoArgsConstructor
public class HealthConcern {


    private String description;


    @NotNull(message = "dateNoticed é obrigatório")
    private LocalDate dateNoticed;


    @NotBlank(message = "treatment é obrigatório")
    private String treatment;


    @NotNull(message = "persisting é obrigatório")
    private Boolean persisting;


    private LocalDate dateResolved;


    @AssertTrue(message = "Quando persisting for false, dateResolved deve ser preenchido; " +
            "quando persisting for true, dateResolved não pode vir definido")
    public boolean isDateResolvedConsistent() {
        if (persisting == null) {

            return true;
        }
        if (persisting) {

            return dateResolved == null;
        } else {

            return dateResolved != null;
        }
    }
}
