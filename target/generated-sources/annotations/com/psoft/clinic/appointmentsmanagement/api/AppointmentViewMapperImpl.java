package com.psoft.clinic.appointmentsmanagement.api;

import com.psoft.clinic.appointmentsmanagement.model.Appointment;
import com.psoft.clinic.model.BaseUser;
import com.psoft.clinic.patientmanagement.model.Patient;
import com.psoft.clinic.physiciansmanagement.model.Physician;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-01T16:20:43+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Amazon.com Inc.)"
)
@Component
public class AppointmentViewMapperImpl implements AppointmentViewMapper {

    @Override
    public AppointmentView toAppointmentView(Appointment appointment) {
        if ( appointment == null ) {
            return null;
        }

        AppointmentView appointmentView = new AppointmentView();

        appointmentView.setDate( appointment.getDate() );
        appointmentView.setStartTime( appointment.getStartTime() );
        appointmentView.setPatientFullName( appointmentPatientBaseUserFullName( appointment ) );
        appointmentView.setPhysicianFullName( appointmentPhysicianBaseUserFullName( appointment ) );
        appointmentView.setEndTime( appointment.getEndTime() );

        appointmentView.setConsultationType( appointment.getConsultationType().name() );

        return appointmentView;
    }

    @Override
    public List<AppointmentView> toAppointmentView(List<Appointment> appointments) {
        if ( appointments == null ) {
            return null;
        }

        List<AppointmentView> list = new ArrayList<AppointmentView>( appointments.size() );
        for ( Appointment appointment : appointments ) {
            list.add( toAppointmentView( appointment ) );
        }

        return list;
    }

    private String appointmentPatientBaseUserFullName(Appointment appointment) {
        if ( appointment == null ) {
            return null;
        }
        Patient patient = appointment.getPatient();
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

    private String appointmentPhysicianBaseUserFullName(Appointment appointment) {
        if ( appointment == null ) {
            return null;
        }
        Physician physician = appointment.getPhysician();
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
}
