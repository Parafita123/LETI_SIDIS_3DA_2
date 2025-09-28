package com.psoft.clinic.adminmanagement.services;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAdminRequest {
    @NotNull
    private String username;

    @NotNull @Size(min = 6)
    private String password;

    @NotNull
    private String fullName;


}