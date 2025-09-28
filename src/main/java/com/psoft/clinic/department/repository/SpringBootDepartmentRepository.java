package com.psoft.clinic.department.repository;

import com.psoft.clinic.department.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringBootDepartmentRepository extends JpaRepository <Department, Long> {
    Optional<Department> findBySigla(String sigla);
}
