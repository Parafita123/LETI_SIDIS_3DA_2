package com.LETI_SIDIS_3DA_2.physician_service.repository;

import com.LETI_SIDIS_3DA_2.physician_service.domain.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, Long> {
    Optional<Specialization> findByName(String name);
}
