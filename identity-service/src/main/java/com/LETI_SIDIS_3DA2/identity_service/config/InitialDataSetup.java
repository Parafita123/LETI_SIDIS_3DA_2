package com.LETI_SIDIS_3DA2.identity_service.config;

import com.LETI_SIDIS_3DA2.identity_service.domain.User;
import com.LETI_SIDIS_3DA2.identity_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Component
public class InitialDataSetup implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println(">>> Executing Initial Data Setup for Identity Service...");

        // Cria todos os utilizadores de teste necessários para o sistema
        createUserIfNotFound("admin", "adminpass", "ADMIN", "admin@example.com", "000000000");
        createUserIfNotFound("patient1", "patientpass", "PATIENT", "patient1@example.com", "111222333");
        createUserIfNotFound("dr.silva", "silvapass", "PHYSICIAN", "dr.silva@clinic.com", "555666777");
        // Adiciona mais utilizadores de teste aqui conforme necessário...

        System.out.println(">>> Identity Service Initial Data Setup Finished.");
    }

    private void createUserIfNotFound(String username, String rawPassword, String role, String email, String phoneNumber) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isEmpty()) {
            String encodedPassword = passwordEncoder.encode(rawPassword);
            User newUser = new User(username, encodedPassword, role, email, phoneNumber);
            userRepository.save(newUser);
            System.out.println("Created User: " + username + " with role " + role);
        } else {
            System.out.println("User: " + username + " already exists. Skipping creation.");
        }
    }
}
