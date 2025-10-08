package com.psoft2024._5.grupo1.projeto_psoft.configuration;

import com.psoft2024._5.grupo1.projeto_psoft.domain.*;
import com.psoft2024._5.grupo1.projeto_psoft.repository.*;
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

    @Autowired private SpecializationRepository specializationRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PhysicianRepository physicianRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private ConsultasRepository consultasRepository; // Nome correto do repositório
    @Autowired private ConsultasRegistoRepository consultasRegistoRepository; // Nome correto do repositório

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

        // --- Specializations ---
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

        // --- Departments ---
        List<Department> departments = new ArrayList<>();
        departments.add(createDepartmentIfNotFound("Departamento de Cardiologia", "DCAR"));
        departments.add(createDepartmentIfNotFound("Departamento de Neurologia", "DNRO"));
        departments.add(createDepartmentIfNotFound("Departamento de Cardiologia", "DCAR"));
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



        // --- Physicians (10) ---
        List<Physician> physicians = new ArrayList<>();
        String[] firstNames = {"João", "Maria", "Carlos", "Ana", "Pedro", "Sofia", "Luís", "Beatriz", "Tiago", "Mariana"};
        String[] lastNames = {"Silva", "Santos", "Pereira", "Ferreira", "Oliveira", "Costa", "Rodrigues", "Martins", "Gomes", "Carvalho"};

        for (int i = 0; i < 10; i++) {
            String fullName = "Dr(a). " + firstNames[i] + " " + lastNames[i];
            Specialization spec = specializations.get(random.nextInt(specializations.size()));
            Department dept = departments.get(random.nextInt(departments.size()));
            String email = firstNames[i].toLowerCase() + "." + lastNames[i].toLowerCase() + (i+1) + "@clinic.com"; // Adiciona (i+1) para garantir unicidade se houver nomes repetidos
            String phone = "9" + String.format("%02d", i+10) + String.format("%06d", random.nextInt(1000000)); // Garante telefones mais únicos
            LocalTime startTime = LocalTime.of(8 + random.nextInt(2), random.nextInt(4) * 15);
            LocalTime endTime = startTime.plusHours(8);
            String desc = random.nextBoolean() ? "Médico especialista em " + spec.getName() : null;
            physicians.add(createPhysicianIfNotFound(fullName, spec, dept, email, phone, "Rua Exemplo " + (i+1) + ", Cidade", startTime, endTime, desc));
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


        // --- Appointments (Consultas - 15) ---
        List<Consulta> consultas = new ArrayList<>();
        String[] consultationTypes = {"Rotina", "Acompanhamento", "Urgência Leve", "Primeira Consulta", "Resultado Exames"};
        String[] appointmentStatus = {"SCHEDULED", "COMPLETED", "CANCELLED"};

        for (int i = 0; i < 15; i++) {
            if (patients.isEmpty() || physicians.isEmpty()) continue; // Evita erro se não houver pacientes/médicos
            Patient patient = patients.get(random.nextInt(patients.size()));
            Physician physician = physicians.get(random.nextInt(physicians.size()));
            LocalDateTime dateTime = LocalDateTime.now().minusDays(random.nextInt(60) - 30)
                    .withHour(9 + random.nextInt(8)).withMinute(random.nextInt(4) * 15);
            Integer duration = 30 + random.nextInt(4) * 15;
            String consultationType = consultationTypes[random.nextInt(consultationTypes.length)];
            String status = appointmentStatus[random.nextInt(appointmentStatus.length)];
            String notes = random.nextBoolean() ? "Notas para consulta " + (i+1) : null;

            consultas.add(createConsultaIfNotFound(patient, physician, dateTime, duration, consultationType, status, notes));
        }

        // --- Appointment Records (ConsultaRegistos - 10, para consultas COMPLETED) ---
        int recordsCreated = 0;
        for (Consulta consulta : consultas) {
            if (consulta == null) continue; // Segurança extra
            if (recordsCreated >= 10) break;

            if ("COMPLETED".equals(consulta.getStatus())) {
                String diagnosis = "Diagnóstico para consulta " + consulta.getId() + ": Condição " + (char)('A' + random.nextInt(5));
                String treatment = "Tratamento recomendado: Procedimento " + (char)('X' + random.nextInt(3));
                String prescriptions = random.nextBoolean() ? "Medicação X, Y, Z" : null;
                LocalDateTime createdAt = consulta.getDateTime().plusMinutes(consulta.getDuration() + random.nextInt(30));

                createConsultaRegistoIfNotFound(consulta, diagnosis, treatment, prescriptions, createdAt);
                recordsCreated++;
            }
        }


        System.out.println(">>> Expanded Initial Data Setup Finished.");
    }

    // --- Métodos Helper com Lógica "If Not Found" ---

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
            System.out.println("User: " + username + " already exists. Returning existing user.");
            return existingUser.get();
        }
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User newUser = new User(username, encodedPassword, role, email, phoneNumber);
        return userRepository.save(newUser);
    }
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

    private Consulta createConsultaIfNotFound(Patient patient, Physician physician, LocalDateTime dateTime,
                                              Integer duration, String consultationType, String status, String notes) {
        // Verifica por paciente, médico e data/hora
        return consultasRepository.findByPatientAndPhysicianAndDateTime(patient, physician, dateTime).orElseGet(() -> {
            System.out.println("Creating Appointment for " + patient.getFullName() + " with " + physician.getFullName() + " at " + dateTime);
            return consultasRepository.save(new Consulta(patient, physician, dateTime, duration, consultationType, status, notes));
        });
    }

    private ConsultaRegisto createConsultaRegistoIfNotFound(Consulta consulta, String diagnosis, String treatment, String prescriptions, LocalDateTime createdAt) {
        // Verifica se já existe um registo para esta consulta
        return consultasRegistoRepository.findByConsulta(consulta).orElseGet(() -> {
            System.out.println("Creating Appointment Record for Appointment ID: " + consulta.getId());
            return consultasRegistoRepository.save(new ConsultaRegisto(consulta, diagnosis, treatment, prescriptions, createdAt));
        });
    }
}