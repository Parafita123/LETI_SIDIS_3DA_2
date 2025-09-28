package com.psoft.clinic.physiciansmanagement.api;

import com.psoft.clinic.department.model.Department;
import com.psoft.clinic.model.Address;
import com.psoft.clinic.model.BaseUser;
import com.psoft.clinic.model.ContactInfo;
import com.psoft.clinic.model.WorkingHours;
import com.psoft.clinic.physiciansmanagement.model.Physician;
import com.psoft.clinic.speciality.model.Speciality;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-13T16:01:44+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.3 (JetBrains s.r.o.)"
)
@Component
public class PhysicianViewMapperImpl implements PhysicianViewMapper {

    private final DateTimeFormatter dateTimeFormatter_HH_mm_168697690 = DateTimeFormatter.ofPattern( "HH:mm" );

    @Override
    public PhysicianView toPhysicianView(Physician physician) {
        if ( physician == null ) {
            return null;
        }

        PhysicianView physicianView = new PhysicianView();

        physicianView.setId( physician.getId() );
        physicianView.setFullName( physicianBaseUserFullName( physician ) );
        physicianView.setEmail( physicianContactInfoEmail( physician ) );
        physicianView.setStreet( physicianContactInfoAddressStreet( physician ) );
        physicianView.setCity( physicianContactInfoAddressCity( physician ) );
        physicianView.setDistrict( physicianContactInfoAddressDistrict( physician ) );
        physicianView.setZip( physicianContactInfoAddressZip( physician ) );
        physicianView.setCountry( physicianContactInfoAddressCountry( physician ) );
        physicianView.setDepartmentSigla( physicianDepartmentSigla( physician ) );
        physicianView.setSpeciality( physicianSpecialityAcronym( physician ) );
        LocalTime startTime = physicianWorkingHoursStartTime( physician );
        if ( startTime != null ) {
            physicianView.setStartTime( dateTimeFormatter_HH_mm_168697690.format( startTime ) );
        }
        LocalTime endTime = physicianWorkingHoursEndTime( physician );
        if ( endTime != null ) {
            physicianView.setEndTime( dateTimeFormatter_HH_mm_168697690.format( endTime ) );
        }

        physicianView.setPhoneNumber( physician.getContactInfo().getPhones()!=null && !physician.getContactInfo().getPhones().isEmpty() ? physician.getContactInfo().getPhones().get(0).getNumber() : null );
        physicianView.setPhoneType( physician.getContactInfo().getPhones()!=null && !physician.getContactInfo().getPhones().isEmpty() ? physician.getContactInfo().getPhones().get(0).getType() : null );
        physicianView.setCreated_At( physician.getBaseUser().getCreatedAt() != null ? physician.getBaseUser().getCreatedAt().toString() : null );
        physicianView.setModified_At( physician.getModifiedAt() != null ? physician.getModifiedAt().toString() : null );

        return physicianView;
    }

    private String physicianBaseUserFullName(Physician physician) {
        if ( physician == null ) {
            return null;
        }
        BaseUser baseUser = physician.getBaseUser();
        if ( baseUser == null ) {
            return null;
        }
        String fullName = baseUser.getFullName();
        if ( fullName == null ) {
            return null;
        }
        return fullName;
    }

    private String physicianContactInfoEmail(Physician physician) {
        if ( physician == null ) {
            return null;
        }
        ContactInfo contactInfo = physician.getContactInfo();
        if ( contactInfo == null ) {
            return null;
        }
        String email = contactInfo.getEmail();
        if ( email == null ) {
            return null;
        }
        return email;
    }

    private String physicianContactInfoAddressStreet(Physician physician) {
        if ( physician == null ) {
            return null;
        }
        ContactInfo contactInfo = physician.getContactInfo();
        if ( contactInfo == null ) {
            return null;
        }
        Address address = contactInfo.getAddress();
        if ( address == null ) {
            return null;
        }
        String street = address.getStreet();
        if ( street == null ) {
            return null;
        }
        return street;
    }

    private String physicianContactInfoAddressCity(Physician physician) {
        if ( physician == null ) {
            return null;
        }
        ContactInfo contactInfo = physician.getContactInfo();
        if ( contactInfo == null ) {
            return null;
        }
        Address address = contactInfo.getAddress();
        if ( address == null ) {
            return null;
        }
        String city = address.getCity();
        if ( city == null ) {
            return null;
        }
        return city;
    }

    private String physicianContactInfoAddressDistrict(Physician physician) {
        if ( physician == null ) {
            return null;
        }
        ContactInfo contactInfo = physician.getContactInfo();
        if ( contactInfo == null ) {
            return null;
        }
        Address address = contactInfo.getAddress();
        if ( address == null ) {
            return null;
        }
        String district = address.getDistrict();
        if ( district == null ) {
            return null;
        }
        return district;
    }

    private String physicianContactInfoAddressZip(Physician physician) {
        if ( physician == null ) {
            return null;
        }
        ContactInfo contactInfo = physician.getContactInfo();
        if ( contactInfo == null ) {
            return null;
        }
        Address address = contactInfo.getAddress();
        if ( address == null ) {
            return null;
        }
        String zip = address.getZip();
        if ( zip == null ) {
            return null;
        }
        return zip;
    }

    private String physicianContactInfoAddressCountry(Physician physician) {
        if ( physician == null ) {
            return null;
        }
        ContactInfo contactInfo = physician.getContactInfo();
        if ( contactInfo == null ) {
            return null;
        }
        Address address = contactInfo.getAddress();
        if ( address == null ) {
            return null;
        }
        String country = address.getCountry();
        if ( country == null ) {
            return null;
        }
        return country;
    }

    private String physicianDepartmentSigla(Physician physician) {
        if ( physician == null ) {
            return null;
        }
        Department department = physician.getDepartment();
        if ( department == null ) {
            return null;
        }
        String sigla = department.getSigla();
        if ( sigla == null ) {
            return null;
        }
        return sigla;
    }

    private String physicianSpecialityAcronym(Physician physician) {
        if ( physician == null ) {
            return null;
        }
        Speciality speciality = physician.getSpeciality();
        if ( speciality == null ) {
            return null;
        }
        String acronym = speciality.getAcronym();
        if ( acronym == null ) {
            return null;
        }
        return acronym;
    }

    private LocalTime physicianWorkingHoursStartTime(Physician physician) {
        if ( physician == null ) {
            return null;
        }
        WorkingHours workingHours = physician.getWorkingHours();
        if ( workingHours == null ) {
            return null;
        }
        LocalTime startTime = workingHours.getStartTime();
        if ( startTime == null ) {
            return null;
        }
        return startTime;
    }

    private LocalTime physicianWorkingHoursEndTime(Physician physician) {
        if ( physician == null ) {
            return null;
        }
        WorkingHours workingHours = physician.getWorkingHours();
        if ( workingHours == null ) {
            return null;
        }
        LocalTime endTime = workingHours.getEndTime();
        if ( endTime == null ) {
            return null;
        }
        return endTime;
    }
}
