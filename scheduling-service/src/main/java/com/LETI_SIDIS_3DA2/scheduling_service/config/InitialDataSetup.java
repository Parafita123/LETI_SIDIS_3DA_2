package com.LETI_SIDIS_3DA2.scheduling_service.config;

// IMPORTS NECESSÁRIOS PARA ESTE SERVIÇO
import com.LETI_SIDIS_3DA2.scheduling_service.domain.Consulta;
import com.LETI_SIDIS_3DA2.scheduling_service.domain.User;
import com.LETI_SIDIS_3DA2.scheduling_service.repository.ConsultaRepository;
import com.LETI_SIDIS_3DA2.scheduling_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Component
public class InitialDataSetup implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConsultaRepository consultaRepository; // Repositório deste serviço

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> Executing Initial Data Setup for Scheduling Service...");

        // --- Utilizadores de Teste (para segurança local) ---
        createUserIfNotFound("admin", "adminpass", "ADMIN", "admin@example.com", "000000000");
        createUserIfNotFound("patient1", "patientpass", "PATIENT", "patient1@example.com", "111222333");
        createUserIfNotFound("dr.silva", "silvapass", "PHYSICIAN", "dr.silva@clinic.com", "555666777");

        // --- Consultas de Teste (Appointments) ---
        // Vamos criar 15 consultas usando IDs "hardcoded" (fictícios) para pacientes e médicos.
        // Assumimos que o Patient Service tem pacientes com IDs de 1 a 20.
        // Assumimos que o Physician Service tem médicos com IDs de 1 a 10.
        String[] consultationTypes = {"Rotina", "Acompanhamento", "Urgência Leve", "Primeira Consulta", "Resultado Exames"};
        String[] appointmentStatus = {"SCHEDULED", "COMPLETED", "CANCELLED"};

        for (int i = 0; i < 15; i++) {
            Long patientId = (long) (1 + random.nextInt(20)); // Paciente ID entre 1 e 20
            Long physicianId = (long) (1 + random.nextInt(10)); // Médico ID entre 1 e 10
            LocalDateTime dateTime = LocalDateTime.now().minusDays(random.nextInt(60) - 30) // +/- 30 dias
                    .withHour(9 + random.nextInt(8)).withMinute(random.nextInt(4) * 15);
            Integer duration = 30 + random.nextInt(4) * 15; // 30, 45, 60, 75 mins
            String consultationType = consultationTypes[random.nextInt(consultationTypes.length)];
            String status = appointmentStatus[random.nextInt(appointmentStatus.length)];
            String notes = random.nextBoolean() ? "Notas para consulta de teste " + (i + 1) : null;

            createConsultaIfNotFound(patientId, physicianId, dateTime, duration, consultationType, status, notes);
        }

        System.out.println(">>> Scheduling Service Initial Data Setup Finished.");
    }

    // --- Métodos Helper ---

    private void createUserIfNotFound(String username, String rawPassword, String role,
                                      String email, String phoneNumber) {
        // 1º: tenta ver se já existe
        userRepository.findByUsername(username).ifPresentOrElse(existing -> {
            System.out.println("User " + username + " já existe (query). Id = " + existing.getId());
        }, () -> {
            // 2º: se não existir, tenta criar
            try {
                String encodedPassword = passwordEncoder.encode(rawPassword);
                User newUser = new User(username, encodedPassword, role, email, phoneNumber);
                userRepository.save(newUser);
                System.out.println("Created Test User: " + username);
            } catch (org.springframework.dao.DataIntegrityViolationException ex) {
                // Corrida / dados antigos na DB
                System.out.println("User " + username + " já existe (constraint DB). Ignorando.");
            }
        });
    }

    private void createConsultaIfNotFound(Long patientId, Long physicianId, LocalDateTime dateTime,
                                          Integer duration, String consultationType, String status, String notes) {
        // Para simplificar, vamos verificar apenas pela combinação de médico e data/hora para evitar
        // duplicados exatos gerados pelo Random. Num sistema real, isto seria mais robusto.
        if (consultaRepository.findByPhysicianIdAndDateTime(physicianId, dateTime).isEmpty()) {
            Consulta novaConsulta = new Consulta(patientId, physicianId, dateTime, duration, consultationType, status, notes);
            consultaRepository.save(novaConsulta);
            System.out.println("Created Test Appointment for patient " + patientId + " with physician " + physicianId + " at " + dateTime);
        }
    }
}