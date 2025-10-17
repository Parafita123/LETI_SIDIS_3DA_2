package com.psoft2024._5.grupo1.projeto_psoft.repository;

import com.psoft2024._5.grupo1.projeto_psoft.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByFullNameContainingIgnoreCase(String fullName);
    Optional<Patient> findByEmail(String email);


}
