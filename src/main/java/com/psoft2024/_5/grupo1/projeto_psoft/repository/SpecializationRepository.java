package com.psoft2024._5.grupo1.projeto_psoft.repository;

import com.psoft2024._5.grupo1.projeto_psoft.domain.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, Long> {
    Optional<Specialization> findByName(String name);
}
