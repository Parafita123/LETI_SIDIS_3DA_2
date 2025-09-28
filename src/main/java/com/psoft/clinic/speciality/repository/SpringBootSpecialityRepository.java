package com.psoft.clinic.speciality.repository;

import com.psoft.clinic.speciality.model.Speciality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringBootSpecialityRepository extends JpaRepository <Speciality, Long> {

    Optional<Speciality> findByName( String name);
}
