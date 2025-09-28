package com.psoft.clinic.physiciansmanagement.repository;


import com.psoft.clinic.physiciansmanagement.model.Physician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringBootPhysicianRepository extends JpaRepository<Physician, Long> {
    Optional<Physician> findByBaseUserUsername(String username);
    Optional <Physician> findByBaseUserFullName(String fullName);
    Optional<Physician> findById(Long id);

        @Query("""
        SELECT p
          FROM Physician p
         WHERE LOWER(p.baseUser.fullName) LIKE LOWER(CONCAT('%', :term, '%'))
            OR LOWER(p.speciality.name) LIKE LOWER(CONCAT('%', :term, '%'))
    """)
        List<Physician> searchByFullNameOrSpeciality(@Param("term") String term);
}
