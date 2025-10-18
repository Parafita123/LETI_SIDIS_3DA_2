package com.LETI_SIDIS_3DA_2.clinical_records_service.config;

import com.LETI_SIDIS_3DA_2.clinical_records_service.domain.ConsultaRegisto;
import com.LETI_SIDIS_3DA_2.clinical_records_service.domain.User;
import com.LETI_SIDIS_3DA_2.clinical_records_service.repository.ConsultaRegistoRepository;
import com.LETI_SIDIS_3DA_2.clinical_records_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class InitialDataSetup implements CommandLineRunner {

    @Autowired
    private ConsultaRegistoRepository recordRepository;

    @Autowired
    private UserRepository userRepository; // Para utilizadores de teste

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println(">>> Executing Initial Data Setup for Clinical Records Service...");

        // --- Utilizadores de Teste (para que a segurança funcione) ---
        // Precisamos de um PHYSICIAN para testar o POST, e um PATIENT/ADMIN para testar o GET.
        createUserIfNotFound("dr.silva", "silvapass", "PHYSICIAN", "dr.silva@clinic.com", "555666777");
        createUserIfNotFound("admin", "adminpass", "ADMIN", "admin@example.com", "000000000");
        createUserIfNotFound("patient1", "patientpass", "PATIENT", "patient1@example.com", "111222333");

        // --- Registos de Consulta de Teste ---
        // Vamos criar alguns registos para consultas fictícias com IDs 1, 2 e 3
        createRecordIfNotFound(
                1L,
                "Enxaqueca tensional recorrente.",
                "Manter medicação analgésica SOS. Recomenda-se técnicas de relaxamento e evitar gatilhos de stress.",
                "Paracetamol 1g, se necessário. Limite de 3 por dia.",
                LocalDateTime.now().minusDays(10)
        );

        createRecordIfNotFound(
                2L,
                "Hipertensão arterial estágio 1, controlada.",
                "Continuar com a medicação atual. Monitorizar a pressão arterial duas vezes por semana. Dieta com baixo teor de sódio.",
                "Lisinopril 10mg, 1 comprimido por dia.",
                LocalDateTime.now().minusDays(5)
        );

        createRecordIfNotFound(
                3L,
                "Controlo de rotina, sem queixas.",
                "Manter estilo de vida saudável. Regressar para check-up anual.",
                null, // Sem prescrições
                LocalDateTime.now().minusDays(2)
        );

        System.out.println(">>> Clinical Records Service Initial Data Setup Finished.");
    }

    // --- Métodos Helper ---

    private void createRecordIfNotFound(Long consultaId, String diagnosis, String treatment, String prescriptions, LocalDateTime createdAt) {
        if (recordRepository.findByConsultaId(consultaId).isEmpty()) {
            ConsultaRegisto record = new ConsultaRegisto(
                    consultaId,
                    diagnosis,
                    treatment,
                    prescriptions,
                    createdAt
            );
            recordRepository.save(record);
            System.out.println("Created clinical record for appointment ID: " + consultaId);
        } else {
            System.out.println("Clinical record for appointment ID: " + consultaId + " already exists. Skipping.");
        }
    }

    private void createUserIfNotFound(String username, String rawPassword, String role, String email, String phoneNumber) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            System.out.println("Test User: " + username + " already exists. Skipping.");
            return;
        }
        String encodedPassword = passwordEncoder.encode(rawPassword);
        // Garante que o construtor de User que estás a usar aceita String para o 'role'
        User newUser = new User(username, encodedPassword, role, email, phoneNumber);
        userRepository.save(newUser);
        System.out.println("Created Test User: " + username);
    }
}