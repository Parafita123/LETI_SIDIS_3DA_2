package com.psoft.clinic.physiciansmanagement.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Setter
@Getter
public class UpdatePhysicianRequest {
    private String fullName;
    private String username;
    private String password;
    private String email;
    private String street;
    private String city;
    private String district;
    private String zip;
    private String country;
    private String phoneNumber;
    private String phoneType;
    private String departmentSigla;
    private String speciality;
    private String startTime;
    private String endTime;

}
