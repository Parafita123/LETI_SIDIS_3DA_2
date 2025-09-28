package com.psoft.clinic.physiciansmanagement.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.psoft.clinic.physiciansmanagement.model.Physician;

@Mapper(componentModel = "spring")
public interface PhysicianViewMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "baseUser.fullName", target = "fullName")

    // Contato
    @Mapping(source = "contactInfo.email", target = "email")
    @Mapping(source = "contactInfo.address.street", target = "street")
    @Mapping(source = "contactInfo.address.city", target = "city")
    @Mapping(source = "contactInfo.address.district", target = "district")
    @Mapping(source = "contactInfo.address.zip", target = "zip")
    @Mapping(source = "contactInfo.address.country", target = "country")
    @Mapping(target = "phoneNumber",
            expression = "java(physician.getContactInfo().getPhones()!=null && !physician.getContactInfo().getPhones().isEmpty() ? physician.getContactInfo().getPhones().get(0).getNumber() : null)")
    @Mapping(target = "phoneType",
            expression = "java(physician.getContactInfo().getPhones()!=null && !physician.getContactInfo().getPhones().isEmpty() ? physician.getContactInfo().getPhones().get(0).getType() : null)")

    // Dados médicos
    @Mapping(source = "department.sigla", target = "departmentSigla")
    @Mapping(source = "speciality.acronym", target = "speciality")
    @Mapping(source = "workingHours.startTime", target = "startTime", dateFormat = "HH:mm")
    @Mapping(source = "workingHours.endTime",   target = "endTime",   dateFormat = "HH:mm")



    @Mapping(target = "created_At",
            expression = "java(physician.getBaseUser().getCreatedAt() != null ? physician.getBaseUser().getCreatedAt().toString() : null)")
    @Mapping(target = "modified_At",
            expression = "java(physician.getModifiedAt() != null ? physician.getModifiedAt().toString() : null)")



    PhysicianView toPhysicianView(Physician physician);
}
