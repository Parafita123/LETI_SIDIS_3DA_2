package com.psoft.clinic.patientmanagement.repository;

import com.psoft.clinic.patientmanagement.model.Patient;
import com.psoft.clinic.physiciansmanagement.model.Physician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringBootPatientRepository extends JpaRepository<Patient, String> {

    boolean existsByBaseUserUsername(String username);

    Optional<Patient> findByBaseUserUsername(String username);

    @Query("""
        SELECT p
          FROM Patient p
         WHERE LOWER(p.baseUser.fullName) LIKE LOWER(CONCAT('%', :term, '%'))
    """)
    List<Patient> searchByFullName(@Param("term") String term);

    Optional<Patient> findByBaseUserFullName(String patientFullName);

    @Query(value = "SELECT nextval('patient_seq')", nativeQuery = true)
    long getNextPatientSeq();


    boolean existsByNsns(String nsns);

}
