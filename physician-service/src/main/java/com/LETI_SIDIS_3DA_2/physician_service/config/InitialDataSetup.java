package com.LETI_SIDIS_3DA_2.physician_service.config; // Pacote do teu novo serviço

import com.LETI_SIDIS_3DA_2.physician_service.domain.Department;
import com.LETI_SIDIS_3DA_2.physician_service.domain.Physician;
import com.LETI_SIDIS_3DA_2.physician_service.domain.Specialization;
import com.LETI_SIDIS_3DA_2.physician_service.domain.User; // Assumindo que User também foi copiado para este serviço
import com.LETI_SIDIS_3DA_2.physician_service.repository.DepartmentRepository;
import com.LETI_SIDIS_3DA_2.physician_service.repository.PhysicianRepository;
import com.LETI_SIDIS_3DA_2.physician_service.repository.SpecializationRepository;
import com.LETI_SIDIS_3DA_2.physician_service.repository.UserRepository; // Necessário para os Users de teste
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
public class InitialDataSetup implements CommandLineRunner {

    @Autowired private SpecializationRepository specializationRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private PhysicianRepository physicianRepository;
    @Autowired private UserRepository userRepository; // Mantém para testes locais de segurança

    @Autowired private PasswordEncoder passwordEncoder;

    private Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println(">>> Executing Initial Data Setup for Physician Service...");

        // --- Users (para fins de teste de segurança local) ---
        createUserIfNotFound("admin", "adminpass", "ADMIN", "admin@example.com", "000000000");
        createUserIfNotFound("patient1", "patientpass", "PATIENT", "patient1@example.com", "111222333");

        // --- Specializations (dados mestre deste serviço) ---
        List<Specialization> specializations = new ArrayList<>();
        specializations.add(createSpecializationIfNotFound("Cardiologia", "Problemas relacionados com o coração."));
        specializations.add(createSpecializationIfNotFound("Neurologia", "Problemas relacinados ao cérebro ou neurónios."));
        specializations.add(createSpecializationIfNotFound("Ortopedia", "Tratamento de ossos e articulações."));
        specializations.add(createSpecializationIfNotFound("Ginecologia", "Cuidados de saúde da mulher."));
        specializations.add(createSpecializationIfNotFound("Oftalmologia", "Problemas relacionados com os olhos."));
        specializations.add(createSpecializationIfNotFound("Psiquiatria", "Saúde mental e distúrbios psicológicos."));
        specializations.add(createSpecializationIfNotFound("Endocrinologia", "Doenças hormonais e do sistema endócrino."));
        specializations.add(createSpecializationIfNotFound("Urologia", "Trato urinário e sistema reprodutor masculino."));
        specializations.add(createSpecializationIfNotFound("Gastroenterologia", "Doenças do sistema digestivo."));
        specializations.add(createSpecializationIfNotFound("Oncologia", "Diagnóstico e tratamento do cancro."));
        specializations.add(createSpecializationIfNotFound("Pediatria", "Auxílio de jovens."));

        // --- Departments (dados mestre deste serviço) ---
        List<Department> departments = new ArrayList<>();
        departments.add(createDepartmentIfNotFound("Departamento de Cardiologia", "DCAR"));
        departments.add(createDepartmentIfNotFound("Departamento de Neurologia", "DNRO"));
        departments.add(createDepartmentIfNotFound("Departamento de Ortopedia", "DOR"));
        departments.add(createDepartmentIfNotFound("Departamento de Ginecologia", "DGIN"));
        departments.add(createDepartmentIfNotFound("Departamento de Oftalmologia", "DOFT"));
        departments.add(createDepartmentIfNotFound("Departamento de Psiquiatria", "DPSI"));
        departments.add(createDepartmentIfNotFound("Departamento de Endocrinologia", "DEND"));
        departments.add(createDepartmentIfNotFound("Departamento de Urologia", "DUR"));
        departments.add(createDepartmentIfNotFound("Departamento de Gastroenterologia", "DGAS"));
        departments.add(createDepartmentIfNotFound("Departamento de Oncologia", "DONC"));
        departments.add(createDepartmentIfNotFound("Departamento de Pediatria", "DP"));
        departments.add(createDepartmentIfNotFound("Departamento de Urgências", "DU"));

        // --- Physicians (entidade principal deste serviço) ---
        List<Physician> physicians = new ArrayList<>();
        String[] firstNames = {"João", "Maria", "Carlos", "Ana", "Pedro", "Sofia", "Luís", "Beatriz", "Tiago", "Mariana"};
        String[] lastNames = {"Silva", "Santos", "Pereira", "Ferreira", "Oliveira", "Costa", "Rodrigues", "Martins", "Gomes", "Carvalho"};

        for (int i = 0; i < 10; i++) {
            String fullName = "Dr(a). " + firstNames[i] + " " + lastNames[i];
            Specialization spec = specializations.get(random.nextInt(specializations.size()));
            Department dept = departments.get(random.nextInt(departments.size()));
            String email = firstNames[i].toLowerCase() + "." + lastNames[i].toLowerCase() + (i+1) + "@clinic.com";
            String phone = "9" + String.format("%02d", i+10) + String.format("%06d", random.nextInt(1000000));
            LocalTime startTime = LocalTime.of(8 + random.nextInt(2), random.nextInt(4) * 15);
            LocalTime endTime = startTime.plusHours(8);
            String desc = random.nextBoolean() ? "Médico especialista em " + spec.getName() : null;
            physicians.add(createPhysicianIfNotFound(fullName, spec, dept, email, phone, "Rua Exemplo " + (i+1) + ", Cidade", startTime, endTime, desc));
        }

        System.out.println(">>> Physician Service Initial Data Setup Finished.");
    }

    // --- Métodos Helper ---

    private Specialization createSpecializationIfNotFound(String name, String description) {
        return specializationRepository.findByName(name).orElseGet(() -> {
            System.out.println("Creating Specialization: " + name);
            return specializationRepository.save(new Specialization(name, description));
        });
    }

    private Department createDepartmentIfNotFound(String name, String acronym) {
        return departmentRepository.findByAcronym(acronym).orElseGet(() -> {
            System.out.println("Creating Department: " + name + " (" + acronym + ")");
            return departmentRepository.save(new Department(name, acronym));
        });
    }

    private User createUserIfNotFound(String username, String rawPassword, String role, String email, String phoneNumber) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            System.out.println("Test User: " + username + " already exists. Returning existing user.");
            return existingUser.get();
        }
        String encodedPassword = passwordEncoder.encode(rawPassword);
        // Garante que o construtor de User que estás a usar aceita String para o 'role'
        User newUser = new User(username, encodedPassword, role, email, phoneNumber);
        return userRepository.save(newUser);
    }
    // Métodos helper para criar Users de teste
    private User createAdminUserIfNotFound(String u, String p, String e, String ph){ return createUserIfNotFound(u,p,"ADMIN",e,ph); }
    private User createPatientUserIfNotFound(String u, String p, String e, String ph){ return createUserIfNotFound(u,p,"PATIENT",e,ph); }

    private Physician createPhysicianIfNotFound(String fullName, Specialization specialty, Department department,
                                                String email, String phoneNumber, String address,
                                                LocalTime workStartTime, LocalTime workEndTime, String optionalDescription) {
        return physicianRepository.findByEmail(email).orElseGet(() -> {
            System.out.println("Creating Physician: " + fullName);
            return physicianRepository.save(new Physician(fullName, specialty, department, email, phoneNumber, address,
                    workStartTime, workEndTime, optionalDescription));
        });
    }
}