package com.psoft.clinic.patientmanagement.api;

import com.psoft.clinic.appointmentsmanagement.api.AppointmentView;
import com.psoft.clinic.appointmentsmanagement.api.AppointmentViewMapper;
import com.psoft.clinic.appointmentsmanagement.api.AvailableSlotsView;
import com.psoft.clinic.appointmentsmanagement.model.Appointment;
import com.psoft.clinic.appointmentsmanagement.services.AppointmentService;
import com.psoft.clinic.appointmentsmanagement.services.AvailableSlotsRequest;
import com.psoft.clinic.appointmentsmanagement.services.CreateAppointmentRequest;
import com.psoft.clinic.patientmanagement.services.EditPatientNumber;
import com.psoft.clinic.patientmanagement.services.PatientService;
import com.psoft.clinic.physiciansmanagement.api.PhysicianView;
import com.psoft.clinic.physiciansmanagement.api.PhysicianViewMapper;
import com.psoft.clinic.physiciansmanagement.services.PhysicianService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Patient")
@RestController
@RequestMapping("/api/patient")
@PreAuthorize("hasRole('USER_PATIENT')")
@RequiredArgsConstructor
public class patientController {

    private final PhysicianService physicianService;
    private final PatientService patientService;
    private final PhysicianViewMapper physicianViewMapper;
    private final AppointmentService appointmentService;
    private final AppointmentViewMapper appointmentViewMapper;
    private final PatientViewMapper patientViewMapper;

    @GetMapping("/search/physicians")
    public List<PhysicianView> searchPhysicians(@RequestParam("term") String term) {
        var physicians = physicianService.searchByFullNameOrSpeciality(term);
        return physicians.stream()
                .map(physicianViewMapper::toPhysicianView)
                .toList();
    }

    @PostMapping("/available-slots")
    public List<AvailableSlotsView> availableSlots(@RequestBody AvailableSlotsRequest req) {
        return appointmentService
                .getAvailableSlots(req.getDate(), req.getPhysicianFullName())
                .stream()
                .map(AvailableSlotsView::new)
                .collect(Collectors.toList());
    }

    @PostMapping("/appointment/create")
    public ResponseEntity<AppointmentView>  createAppointment(@RequestBody CreateAppointmentRequest request) {
        Appointment ap = appointmentService.P_create(request);
        AppointmentView view = appointmentViewMapper.toAppointmentView(ap);
        return ResponseEntity.status(HttpStatus.CREATED).body(view);
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentView>> getAppointments(
            @RequestParam("physicianName") String physicianName) {

        List<Appointment> appointments =
                appointmentService.getAppointments(physicianName);

        List<AppointmentView> views = appointments.stream()
                .map(appointmentViewMapper::toAppointmentView)
                .collect(Collectors.toList());

        return ResponseEntity.ok(views);
    }


    @GetMapping("/appointments/history")
    public ResponseEntity<List<String>> getAppointmentHistory() {
        List<Appointment> history = appointmentService.getPatientHistory();

        List<String> notes = history.stream()
                .map(Appointment::getDetails)  // só pega as notas
                .toList();


        return ResponseEntity.ok(notes);
    }

    @PutMapping("/edit")
    public ResponseEntity<PatientView> editPhone(
            @Valid @RequestBody EditPatientNumber dto,
            BindingResult br
    ) {

        if (br.hasErrors()) {

            String msg = br.getFieldError().getDefaultMessage();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        var updated = patientService.updatePhoneNumber(username, dto);

        return ResponseEntity.ok(patientViewMapper.toPatientView(updated));
    }


}
