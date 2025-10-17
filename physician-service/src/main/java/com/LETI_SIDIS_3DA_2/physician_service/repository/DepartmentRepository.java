package com.LETI_SIDIS_3DA_2.physician_service.repository;

import com.LETI_SIDIS_3DA_2.physician_service.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByAcronym(String acronym);
    Optional<Department> findByName(String name);
}