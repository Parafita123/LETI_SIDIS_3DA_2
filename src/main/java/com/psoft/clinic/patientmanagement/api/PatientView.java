package com.psoft.clinic.patientmanagement.api;

import com.psoft.clinic.model.Gender;
import com.psoft.clinic.model.PhoneType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientView {
    private String patientId;
    private String nsns;
    private String fullName;
    private Gender gender;
    private String email;
    private String street;
    private String city;
    private String district;
    private String zip;
    private String country;
    private String phoneNumber;
    private PhoneType phoneType;
    private String dateOfBirth;
    private String insuranceProvider;
    private String policyNumber;
    private String consent;
}
