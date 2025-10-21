package com.LETI_SIDIS_3DA2.Patient_Service.repository;

import com.LETI_SIDIS_3DA2.Patient_Service.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByFullNameContainingIgnoreCase(String fullName);
    Optional<Patient> findByEmail(String email);


}
