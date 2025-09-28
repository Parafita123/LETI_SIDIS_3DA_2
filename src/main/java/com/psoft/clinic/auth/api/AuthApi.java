package com.psoft.clinic.auth.api;


import java.util.Optional;
import java.util.stream.Stream;

import com.psoft.clinic.adminmanagement.api.AdminViewMapper;
import com.psoft.clinic.adminmanagement.model.Admin;
import com.psoft.clinic.adminmanagement.repository.SpringBootAdminRepository;
import com.psoft.clinic.configuration.security.JwtUtils;
import com.psoft.clinic.model.Phone;
import com.psoft.clinic.patientmanagement.api.PatientView;
import com.psoft.clinic.patientmanagement.api.PatientViewMapper;
import com.psoft.clinic.patientmanagement.model.Patient;
import com.psoft.clinic.patientmanagement.repository.SpringBootPatientRepository;
import com.psoft.clinic.patientmanagement.services.CreatePatientRequest;
import com.psoft.clinic.patientmanagement.services.PatientService;
import com.psoft.clinic.physiciansmanagement.api.PhysicianViewMapper;
import com.psoft.clinic.physiciansmanagement.model.Physician;
import com.psoft.clinic.physiciansmanagement.repository.SpringBootPhysicianRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;


import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/public")
public class AuthApi {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final SpringBootAdminRepository adminRepo;
    private final SpringBootPatientRepository patientRepo;
    private final SpringBootPhysicianRepository physicianRepo;
    private final AdminViewMapper adminViewMapper;
    private final PatientViewMapper patientViewMapper;
    private final PhysicianViewMapper physicianViewMapper;
    private final PatientService patientService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest req) {
        var userOpt = Stream.<Optional<?>>of(
                        adminRepo.findByBaseUserUsername(req.getUsername()),
                        patientRepo.findByBaseUserUsername(req.getUsername()),
                        physicianRepo.findByBaseUserUsername(req.getUsername())
                )
                .flatMap(Optional::stream)
                .findFirst();

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        Object user = userOpt.get();

        String rawPass = req.getPassword();
        String hashed = switch (user) {
            case Admin a      -> a.getBaseUser().getPassword();
            case Patient p    -> p.getBaseUser().getPassword();
            case Physician ph -> ph.getBaseUser().getPassword();
            default            -> "";
        };
        if (!passwordEncoder.matches(rawPass, hashed)) {
            return ResponseEntity.status(401).build();
        }

        String role = switch (user) {
            case Admin a      -> a.getBaseUser().getRole();
            case Patient p    -> p.getBaseUser().getRole();
            case Physician ph -> ph.getBaseUser().getRole();
            default            -> "";
        };

        Phone phone = (user instanceof Patient) ? ((Patient) user).getPhone() : null;

        String token = jwtUtils.generateToken(
                req.getUsername(),
                role,
                phone
        );
        Object view = switch (user) {
            case Admin a      -> adminViewMapper.toAdminView(a);
            case Patient p    -> patientViewMapper.toPatientView(p);
            case Physician ph -> physicianViewMapper.toPhysicianView(ph);
            default            -> null;
        };

        if (view != null) {
            try {
                view.getClass().getMethod("setToken", String.class)
                        .invoke(view, token);
            } catch (Exception ignored) {}
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(view);
    }



    @PostMapping(
            value = "register",
            consumes = {
                    MediaType.APPLICATION_JSON_VALUE,
                    MediaType.MULTIPART_FORM_DATA_VALUE
            }
    )
    public ResponseEntity<PatientView> create(
            @RequestPart("request") @Valid CreatePatientRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        Patient patient = patientService.create(request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(patientViewMapper.toPatientView(patient));
    }


}
