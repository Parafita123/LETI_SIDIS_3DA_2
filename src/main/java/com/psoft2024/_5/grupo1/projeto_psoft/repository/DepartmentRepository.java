package com.psoft2024._5.grupo1.projeto_psoft.repository;

import com.psoft2024._5.grupo1.projeto_psoft.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByAcronym(String acronym);
    Optional<Department> findByName(String name);
}