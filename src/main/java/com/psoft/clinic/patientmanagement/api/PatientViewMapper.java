package com.psoft.clinic.patientmanagement.api;

import com.psoft.clinic.patientmanagement.model.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientViewMapper {

    @Mapping(source = "patientId", target = "patientId")
    @Mapping(source = "nsns",target = "nsns")
    @Mapping(source = "baseUser.fullName", target = "fullName")
    @Mapping(source = "gender", target = "gender")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "address.street", target = "street")
    @Mapping(source = "address.city", target = "city")
    @Mapping(source = "address.district", target = "district")
    @Mapping(source = "address.zip", target = "zip")
    @Mapping(source = "address.country", target = "country")
    @Mapping(source = "phone.number", target = "phoneNumber")
    @Mapping(source = "phone.type", target = "phoneType")
    @Mapping(source = "dateOfBirth", target = "dateOfBirth", dateFormat = "dd/MM/yyyy")
    @Mapping(source = "insuranceInfo.insuranceprovider", target = "insuranceProvider")
    @Mapping(source = "insuranceInfo.policyNumber", target = "policyNumber")
    @Mapping(source = "dataConsent.consent", target = "consent")
    PatientView toPatientView(Patient patient);
}