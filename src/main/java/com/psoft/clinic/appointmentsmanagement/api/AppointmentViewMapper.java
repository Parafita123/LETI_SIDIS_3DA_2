package com.psoft.clinic.appointmentsmanagement.api;

import com.psoft.clinic.appointmentsmanagement.model.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AppointmentViewMapper {

    @Mapping(source  = "date",  target = "date")
    @Mapping(source  = "startTime", target = "startTime")
    @Mapping(target  = "consultationType",
            expression = "java(appointment.getConsultationType().name())")
    @Mapping(source  = "patient.baseUser.fullName",   target = "patientFullName")
    @Mapping(source  = "physician.baseUser.fullName", target = "physicianFullName")
    AppointmentView toAppointmentView(Appointment appointment);

    List<AppointmentView> toAppointmentView(List<Appointment> appointments);
}
