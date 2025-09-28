package com.psoft.clinic.physiciansmanagement.api;

import java.time.Instant;

import com.psoft.clinic.model.PhoneType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhysicianView {

    private Long id;
    private String fullName;
    // Contato
    private String email;
    private String street;
    private String city;
    private String district;
    private String zip;
    private String country;
    private String phoneNumber;
    private PhoneType phoneType;

    // Dados médicos
    private String departmentSigla;
    private String speciality;
    private String startTime;
    private String endTime;

    private String created_At;
    private String modified_At;
}
