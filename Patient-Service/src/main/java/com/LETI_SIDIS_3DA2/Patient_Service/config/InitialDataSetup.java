package com.LETI_SIDIS_3DA2.Patient_Service.config;

import com.LETI_SIDIS_3DA2.Patient_Service.domain.*;
import com.LETI_SIDIS_3DA2.Patient_Service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
public class InitialDataSetup implements CommandLineRunner {


    @Autowired private UserRepository userRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println(">>> Executing Expanded Initial Data Setup (with IfNotFound for all)...");

        // --- Users ---
        User adminUser = createUserIfNotFound("admin", "adminpass", "ADMIN", "admin@example.com", "000000000");
        List<User> patientUsers = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            patientUsers.add(createUserIfNotFound("patient" + i, "patientpass" + i, "PATIENT", "patient" + i + "@example.com", "111222" + String.format("%03d", i)));
        }


        // --- Patients (20) ---
        List<Patient> patients = new ArrayList<>();
        String[] patientFirstNames = {"Miguel", "Laura", "Diogo", "Inês", "Rui", "Catarina", "Bruno", "Daniela", "André", "Sara", "José", "Cláudia", "Ricardo", "Marta", "Vasco", "Eva", "Nuno", "Telma", "Hugo", "Sónia"};
        String[] patientLastNames = {"Almeida", "Monteiro", "Gonçalves", "Pinto", "Teixeira", "Sousa", "Marques", "Lopes", "Ramos", "Nunes", "Reis", "Mendes", "Barros", "Freitas", "Correia", "Neves", "Campos", "Cardoso", "Pires", "Abreu"};
        String[] insuranceCompanies = {"Seguro Top", "Vida Plena", "Proteção Total", "Saúde Certa", "BemEstar Seguros"};

        for (int i = 0; i < 20; i++) {
            User user = (i < patientUsers.size()) ? patientUsers.get(i) : null;
            String fullName = patientFirstNames[i] + " " + patientLastNames[i];
            String email = patientFirstNames[i].toLowerCase() + "." + patientLastNames[i].toLowerCase() + (i+1) + "@example.com";
            LocalDate birthDate = LocalDate.of(1960 + random.nextInt(45), 1 + random.nextInt(12), 1 + random.nextInt(28));
            String phone = "9" + String.format("%02d", i+30) + String.format("%06d", random.nextInt(1000000)); // Garante telefones mais únicos
            String insuranceCompany = insuranceCompanies[random.nextInt(insuranceCompanies.length)];
            String insurancePolicy = "POL" + String.format("%06d", random.nextInt(1000000));
            LocalDate consentDate = LocalDate.now().minusDays(random.nextInt(365));

            patients.add(createPatientIfNotFound(user, fullName, email, birthDate, phone, insuranceCompany, insurancePolicy, consentDate));
        }


        System.out.println(">>> Expanded Initial Data Setup Finished.");
    }

    // --- Métodos Helper com Lógica "If Not Found" ---


    private User createUserIfNotFound(String username, String rawPassword, String role, String email, String phoneNumber) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            System.out.println("User: " + username + " already exists. Returning existing user.");
            return existingUser.get();
        }
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User newUser = new User(username, encodedPassword, role, email, phoneNumber);
        return userRepository.save(newUser);
    }
    private User createAdminUserIfNotFound(String u, String p, String e, String ph){ return createUserIfNotFound(u,p,"ADMIN",e,ph); }
    private User createPatientUserIfNotFound(String u, String p, String e, String ph){ return createUserIfNotFound(u,p,"PATIENT",e,ph); }


    private Patient createPatientIfNotFound(User user, String fullName, String email, LocalDate birthDateLocal,
                                            String phoneNumber, String insuranceCompany, String insurancePolicyNumber,
                                            LocalDate consentDate) {
        // Verifica por email, que é unique na tua entidade Patient
        return patientRepository.findByEmail(email).orElseGet(() -> {
            Patient patient = new Patient();
            patient.setUser(user);
            patient.setFullName(fullName);
            patient.setName(fullName); // Assumindo que 'name' deve ser o mesmo que 'fullName'
            patient.setEmail(email);
            patient.setBirthDateLocal(birthDateLocal);
            patient.setBirthDate(birthDateLocal.toString()); // Campo string
            patient.setPhoneNumber(phoneNumber);
            patient.setInsuranceCompany(insuranceCompany);
            patient.setInsurancePolicyNumber(insurancePolicyNumber);
            patient.setInsuranceProvider(insuranceCompany); // Assumindo que é o mesmo
            patient.setConsentDate(consentDate);

            System.out.println("Creating Patient: " + fullName);
            return patientRepository.save(patient);
        });
    }
}