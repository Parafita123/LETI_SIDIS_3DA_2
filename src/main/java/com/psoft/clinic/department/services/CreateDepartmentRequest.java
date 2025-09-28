package com.psoft.clinic.department.services;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDepartmentRequest {
    @NotNull@Size(max = 5)
    private String sigla;
    @NotNull
    private String nome;
    @NotNull
    private String descricao;
}
