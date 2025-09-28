package com.psoft.clinic.speciality.services;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateSpecialityRequest {
    @NotNull
    private String name;
    @NotNull
    private String description;
    @NotNull
    @Size(max = 5)
    private String acronym;
}
