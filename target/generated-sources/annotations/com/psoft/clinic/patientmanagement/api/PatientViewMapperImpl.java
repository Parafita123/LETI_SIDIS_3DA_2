package com.psoft.clinic.patientmanagement.api;

import com.psoft.clinic.model.Address;
import com.psoft.clinic.model.BaseUser;
import com.psoft.clinic.model.Phone;
import com.psoft.clinic.model.PhoneType;
import com.psoft.clinic.model.dataConsent;
import com.psoft.clinic.model.insuranceInfo;
import com.psoft.clinic.patientmanagement.model.Patient;
import java.time.format.DateTimeFormatter;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-01T16:20:43+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Amazon.com Inc.)"
)
@Component
public class PatientViewMapperImpl implements PatientViewMapper {

    private final DateTimeFormatter dateTimeFormatter_dd_MM_yyyy_0650712384 = DateTimeFormatter.ofPattern( "dd/MM/yyyy" );

    @Override
    public PatientView toPatientView(Patient patient) {
        if ( patient == null ) {
            return null;
        }

        PatientView patientView = new PatientView();

        patientView.setPatientId( patient.getPatientId() );
        patientView.setNsns( patient.getNsns() );
        patientView.setFullName( patientBaseUserFullName( patient ) );
        patientView.setGender( patient.getGender() );
        patientView.setEmail( patient.getEmail() );
        patientView.setStreet( patientAddressStreet( patient ) );
        patientView.setCity( patientAddressCity( patient ) );
        patientView.setDistrict( patientAddressDistrict( patient ) );
        patientView.setZip( patientAddressZip( patient ) );
        patientView.setCountry( patientAddressCountry( patient ) );
        patientView.setPhoneNumber( patientPhoneNumber( patient ) );
        patientView.setPhoneType( patientPhoneType( patient ) );
        if ( patient.getDateOfBirth() != null ) {
            patientView.setDateOfBirth( dateTimeFormatter_dd_MM_yyyy_0650712384.format( patient.getDateOfBirth() ) );
        }
        patientView.setInsuranceProvider( patientInsuranceInfoInsuranceprovider( patient ) );
        patientView.setPolicyNumber( patientInsuranceInfoPolicyNumber( patient ) );
        patientView.setConsent( patientDataConsentConsent( patient ) );

        return patientView;
    }

    private String patientBaseUserFullName(Patient patient) {
        if ( patient == null ) {
            return null;
        }
        BaseUser baseUser = patient.getBaseUser();
        if ( baseUser == null ) {
            return null;
        }
        String fullName = baseUser.getFullName();
        if ( fullName == null ) {
            return null;
        }
        return fullName;
    }

    private String patientAddressStreet(Patient patient) {
        if ( patient == null ) {
            return null;
        }
        Address address = patient.getAddress();
        if ( address == null ) {
            return null;
        }
        String street = address.getStreet();
        if ( street == null ) {
            return null;
        }
        return street;
    }

    private String patientAddressCity(Patient patient) {
        if ( patient == null ) {
            return null;
        }
        Address address = patient.getAddress();
        if ( address == null ) {
            return null;
        }
        String city = address.getCity();
        if ( city == null ) {
            return null;
        }
        return city;
    }

    private String patientAddressDistrict(Patient patient) {
        if ( patient == null ) {
            return null;
        }
        Address address = patient.getAddress();
        if ( address == null ) {
            return null;
        }
        String district = address.getDistrict();
        if ( district == null ) {
            return null;
        }
        return district;
    }

    private String patientAddressZip(Patient patient) {
        if ( patient == null ) {
            return null;
        }
        Address address = patient.getAddress();
        if ( address == null ) {
            return null;
        }
        String zip = address.getZip();
        if ( zip == null ) {
            return null;
        }
        return zip;
    }

    private String patientAddressCountry(Patient patient) {
        if ( patient == null ) {
            return null;
        }
        Address address = patient.getAddress();
        if ( address == null ) {
            return null;
        }
        String country = address.getCountry();
        if ( country == null ) {
            return null;
        }
        return country;
    }

    private String patientPhoneNumber(Patient patient) {
        if ( patient == null ) {
            return null;
        }
        Phone phone = patient.getPhone();
        if ( phone == null ) {
            return null;
        }
        String number = phone.getNumber();
        if ( number == null ) {
            return null;
        }
        return number;
    }

    private PhoneType patientPhoneType(Patient patient) {
        if ( patient == null ) {
            return null;
        }
        Phone phone = patient.getPhone();
        if ( phone == null ) {
            return null;
        }
        PhoneType type = phone.getType();
        if ( type == null ) {
            return null;
        }
        return type;
    }

    private String patientInsuranceInfoInsuranceprovider(Patient patient) {
        if ( patient == null ) {
            return null;
        }
        insuranceInfo insuranceInfo = patient.getInsuranceInfo();
        if ( insuranceInfo == null ) {
            return null;
        }
        String insuranceprovider = insuranceInfo.getInsuranceprovider();
        if ( insuranceprovider == null ) {
            return null;
        }
        return insuranceprovider;
    }

    private String patientInsuranceInfoPolicyNumber(Patient patient) {
        if ( patient == null ) {
            return null;
        }
        insuranceInfo insuranceInfo = patient.getInsuranceInfo();
        if ( insuranceInfo == null ) {
            return null;
        }
        String policyNumber = insuranceInfo.getPolicyNumber();
        if ( policyNumber == null ) {
            return null;
        }
        return policyNumber;
    }

    private String patientDataConsentConsent(Patient patient) {
        if ( patient == null ) {
            return null;
        }
        dataConsent dataConsent = patient.getDataConsent();
        if ( dataConsent == null ) {
            return null;
        }
        String consent = dataConsent.getConsent();
        if ( consent == null ) {
            return null;
        }
        return consent;
    }
}
