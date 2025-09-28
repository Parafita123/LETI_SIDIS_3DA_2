package com.psoft.clinic.department.services;

import com.psoft.clinic.department.model.Department;
import com.psoft.clinic.department.repository.SpringBootDepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final SpringBootDepartmentRepository departmentRepository;

    public Department create(CreateDepartmentRequest request) {
        Department department = new Department();
        department.setSigla(request.getSigla());
        department.setNome(request.getNome());
        department.setDescricao(request.getDescricao());
        return departmentRepository.save(department);
    }

    public Optional<Department> findBySigla(String sigla) {
        return departmentRepository.findBySigla(sigla);
    }
}
