package com.psoft.clinic.patientmanagement.services;

import com.psoft.clinic.appointmentsmanagement.repository.SpringBootAppointmentRepository;
import com.psoft.clinic.exceptions.*;
import com.psoft.clinic.model.BaseUser;
import com.psoft.clinic.model.*;
import com.psoft.clinic.patientmanagement.services.EditPatientNumber;

import com.psoft.clinic.patientmanagement.services.HealthConcernDTO;
import com.psoft.clinic.patientmanagement.model.HealthConcern;
import com.psoft.clinic.patientmanagement.model.Patient;
import com.psoft.clinic.patientmanagement.repository.SpringBootPatientRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.psoft.clinic.appointmentsmanagement.services.AppointmentService.DATE_FMT;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final SpringBootPatientRepository patientRepository;
    private final SpringBootAppointmentRepository appointmentRepository;

    private final PasswordEncoder passwordEncoder;
    private static final DateTimeFormatter YEAR_FMT = DateTimeFormatter.ofPattern("yyyy");

    private final Path rootLocation = Paths.get(
            System.getProperty("user.dir"),
            "src", "main", "resources", "patient"
    );



    public Patient create(CreatePatientRequest request,
                          MultipartFile file) {
        String year = LocalDate.now().format(YEAR_FMT);

        long seq = patientRepository.getNextPatientSeq();
        String seqFormatted = String.format("%05d", seq);
        String patientId = "P" + year + seqFormatted;

        String username = request.getUsername();
        if (patientRepository.existsByBaseUserUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }

        if (request.getConsent() == null || !Boolean.parseBoolean(request.getConsent())) {
            throw new ConsentRequiredException();
        }

        LocalDate dob = DateValidator.validateDate(request.getDateOfBirth());

        BaseUser baseUser = new BaseUser();
        baseUser.setUsername(username);
        baseUser.setFullName(request.getFullName());
        baseUser.setPassword(passwordEncoder.encode(request.getPassword()));
        baseUser.setRole(Role.USER_PATIENT);


        Address address = new Address();
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setZip(request.getZip());
        address.setCountry(request.getCountry());

        Phone phone = new Phone();
        phone.setNumber(request.getPhoneNumber());
        phone.setType(request.getPhoneType());

        insuranceInfo insInfo = new insuranceInfo();
        insInfo.setInsuranceprovider(request.getInsuranceProvider());
        insInfo.setPolicyNumber(request.getPolicyNumber());

        dataConsent consent = new dataConsent();
        consent.setConsent(request.getConsent());

        Patient patient = new Patient();
        patient.setPatientId(patientId);
        patient.setBaseUser(baseUser);

        String sns = request.getNsns();

        System.out.println("SNS recebido: " + sns);
        System.out.println("Já existe? " + patientRepository.existsByNsns(sns));
        boolean snsExists = patientRepository.existsByNsns(sns);
        System.out.println("SNS existe? " + snsExists);


        if (snsExists) {
            throw new SnsDuplicatedException(sns);
        }


        patient.setNsns(request.getNsns());
        patient.setEmail(request.getEmail());
        patient.setAddress(address);
        patient.setPhone(phone);
        patient.setDateOfBirth(dob);
        patient.setInsuranceInfo(insInfo);
        patient.setDataConsent(consent);
        patient.setGender(Gender.valueOf(request.getGender()));


        String uploadDir = rootLocation.toFile().getAbsolutePath();
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String filename = null;
        String extension = null;

        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType == null
                    || !Set.of("image/jpeg", "image/png", "image/jpg")
                    .contains(contentType.toLowerCase())) {
                throw new InvalidImageFormatException(
                        "Apenas são permitidos ficheiros JPG, JPEG ou PNG"
                );
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename
                        .substring(originalFilename.lastIndexOf('.') + 1)
                        .toLowerCase();
            }
            if (extension == null
                    || !Set.of("jpg", "jpeg", "png").contains(extension)) {
                throw new InvalidImageFormatException(
                        "Extensão inválida: " + extension + ". Só JPG, JPEG e PNG são permitidos."
                );
            }

            filename = username + "." + extension;
            Path destination = Paths.get(uploadDir, filename);
            try {
                file.transferTo(destination.toFile());
            } catch (IOException e) {
                throw new RuntimeException(
                        "Falha ao gravar ficheiro de imagem em " + destination, e
                );
            }
        }

        if (filename != null) {
            patient.setImage(filename);
        }


        if (request.getHealthConcerns() != null) {
            List<HealthConcern> healthConcerns = request.getHealthConcerns().stream()
                    .map(this::convertToHealthConcern)
                    .collect(Collectors.toList());
            patient.setHealthConcerns(healthConcerns);
        }


        // Persiste e retorna


        return patientRepository.save(patient);
    }



    private HealthConcern convertToHealthConcern(HealthConcernDTO dto) {
        HealthConcern healthConcern = new HealthConcern();
        healthConcern.setDescription(dto.getDescription());
        healthConcern.setDateNoticed(LocalDate.parse(dto.getDateNoticed(), DATE_FMT));
        healthConcern.setTreatment(dto.getTreatment());
        healthConcern.setPersisting(dto.getPersisting());

        if (dto.getDateResolved() != null && !dto.getDateResolved().isEmpty()) {
            try {
                healthConcern.setDateResolved(LocalDate.parse(dto.getDateResolved(), DATE_FMT));
            } catch (DateTimeParseException e) {
                // Log the error or handle it appropriately
                System.err.println("Error parsing dateResolved: " + e.getMessage());
                // Potentially set to null or a default value, depending on your requirements
                healthConcern.setDateResolved(null);
            }
        }

        return healthConcern;
    }


    public Optional<Patient> findById(String patientId) {
        return patientRepository.findById(patientId);
    }

    public List<Patient> searchByFullName(String FullName) {
/*
        List<Patient> patients = patientRepository.searchByFullName(FullName);

        if (patients == null || patients.isEmpty()) {
            throw new PatientNotFoundException(FullName);
        }

 */
        return patientRepository.searchByFullName(FullName);
    }






    @Transactional
    public Patient updatePhoneNumber(String username, EditPatientNumber request) {

        Patient existingPatient = patientRepository.findByBaseUserUsername(username)
                .orElseThrow(() -> new PatientNotFoundException(username));

        Phone phone = existingPatient.getPhone();
        phone.setNumber(request.getPhoneNumber());

        existingPatient.setPhone(phone);

        return patientRepository.save(existingPatient);
    }








}



