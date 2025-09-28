package com.psoft.clinic.adminmanagement.api;

import com.psoft.clinic.appointmentsmanagement.api.AppointmentView;
import com.psoft.clinic.appointmentsmanagement.api.AppointmentViewMapper;
import com.psoft.clinic.appointmentsmanagement.model.Appointment;
import com.psoft.clinic.appointmentsmanagement.services.ReportService;
import com.psoft.clinic.appointmentsmanagement.services.MonthlyReportResponse;
import com.psoft.clinic.appointmentsmanagement.services.AgeGroupStatsDto;
import com.psoft.clinic.appointmentsmanagement.services.AppointmentService;
import com.psoft.clinic.appointmentsmanagement.services.RescheduleRequest;
import com.psoft.clinic.appointmentsmanagement.services.CreateAppointmentRequest;
import com.psoft.clinic.appointmentsmanagement.services.PhysicianCount;
import com.psoft.clinic.exceptions.InvalidDateException;
import com.psoft.clinic.exceptions.InvalidImageFormatException;
import com.psoft.clinic.exceptions.UsernameAlreadyExistsException;
import com.psoft.clinic.patientmanagement.api.PatientView;
import com.psoft.clinic.patientmanagement.api.PatientViewMapper;
import com.psoft.clinic.patientmanagement.services.PatientService;
import com.psoft.clinic.physiciansmanagement.services.UpdatePhysicianRequest;
import com.psoft.clinic.physiciansmanagement.api.PhysicianView;
import com.psoft.clinic.physiciansmanagement.api.PhysicianViewMapper;
import com.psoft.clinic.physiciansmanagement.model.Physician;
import com.psoft.clinic.physiciansmanagement.services.CreatePhysicianRequest;
import com.psoft.clinic.physiciansmanagement.services.PhysicianService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Admin")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('USER_ADMIN')")
@RequiredArgsConstructor
public class adminController {

    private final PhysicianService physicianService;
    private final PhysicianViewMapper physicianViewMapper;
    private final PatientService patientService;
    private final PatientViewMapper patientViewMapper;
    private final AppointmentService appointmentService;
    private final AppointmentViewMapper appointmentViewMapper;
    private final ReportService reportService;

    @GetMapping("/search/physician/{id}")
    public PhysicianView searchPhysician(@PathVariable Long id) {
        var physician = physicianService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Physician with id %d not found", id)
                ));
        return physicianViewMapper.toPhysicianView(physician);
    }

    @GetMapping("/search/patient/{patientId}")
    public PatientView searchByPatient(@PathVariable String patientId) {
        var patient = patientService.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Patient with id %d not found", patientId)
                ));
        return patientViewMapper.toPatientView(patient);
    }

    @GetMapping("/search/patient")
    public List<PatientView> searchPatient(@RequestParam("term") String term) {
        var patient = patientService.searchByFullName(term);
        return patient.stream()
                .map(patientViewMapper::toPatientView)
                .toList();
    }

    @PostMapping("/appointment/create")
    public AppointmentView createAppointment(@RequestBody CreateAppointmentRequest request) {
        Appointment ap = appointmentService.create(request);
        return appointmentViewMapper.toAppointmentView(ap);
    }

    /****************************************************************************************************************
     *                                          WP#1B
     ***************************************************************************************************************/
    @PatchMapping(value = "physician/edit/{id}", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<PhysicianView> editPhysician(
            @PathVariable Long id,
            @RequestPart(required = false) UpdatePhysicianRequest updateRequest,
            @RequestPart(required = false) MultipartFile file) {
        try {
            var updated = physicianService.updatePhysician(id, updateRequest, file);
            return ResponseEntity.ok(physicianViewMapper.toPhysicianView(updated));
        }catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PostMapping(
            path = "/physician/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PhysicianView> createPhysician(
            @RequestPart("physician")@Valid CreatePhysicianRequest request,
            @RequestPart(value = "file",required = false) MultipartFile file
    ) {
        try {
            Physician p = physicianService.create(request, file);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(physicianViewMapper.toPhysicianView(p));
        } catch (UsernameAlreadyExistsException |
                 EntityNotFoundException |
                 InvalidDateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);

        }catch (
                InvalidImageFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage(), ex);

        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao criar physician", ex
            );
        }
    }

    @GetMapping("/report/top5")
    public List<PhysicianCount> getTop5Physicians(
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate")   String endDateStr
    ) {
        try {
            return appointmentService.getTop5Physicians(startDateStr, endDateStr);
        } catch (InvalidDateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }



    @GetMapping("/stats/appointments/age-groups")
    public ResponseEntity<List<AgeGroupStatsDto>> getAppointmentStatsByAgeGroup() {
        List<AgeGroupStatsDto> stats = appointmentService.getStatsByPatientAgeGroup();
        return ResponseEntity.ok(stats);
    }

    /****************************************************************************************************************
     *
     ***************************************************************************************************************/

    @GetMapping("/appointment/upcoming")
    public List<AppointmentView> listAppointments() {
        List<Appointment> appointments = appointmentService.getUpcomingAppointments();
        return appointments.stream()
                .map(appointmentViewMapper::toAppointmentView)
                .collect(Collectors.toList());
    }

    @PutMapping("/appointment/{id}/cancel")
    public void cancelAppointment(@PathVariable Long id) {
        appointmentService.cancel(id);
    }

    @PutMapping("/appointment/{id}/reschedule")
    public void rescheduleAppointment(@PathVariable Long id,
                                      @RequestBody RescheduleRequest request) {
        appointmentService.reschedule(id, request.getNewDate(), request.getNewStartTime());
    }

    @GetMapping("/reports/monthly/{year}/{month}")
    public MonthlyReportResponse getMonthlyReport(@PathVariable int year, @PathVariable int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Mês deve ser entre 1 e 12");
        }
        return reportService.generateMonthlyReport(month, year);
    }

    @GetMapping("/admin/physicians/average-duration")
    public ResponseEntity<Map<String, Double>> getAverageDurationPerPhysician() {
        return ResponseEntity.ok(appointmentService.getAverageDurationPerPhysician());
    }

}
