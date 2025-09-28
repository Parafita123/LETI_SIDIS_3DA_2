package com.psoft.clinic.bootstrapping;

import com.psoft.clinic.department.services.CreateDepartmentRequest;
import com.psoft.clinic.department.model.Department;
import com.psoft.clinic.department.services.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("bootstrap")
@RequiredArgsConstructor
@Order(1)
public class departmentBootStrapper implements CommandLineRunner {

    private final DepartmentService departmentService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        createIfNotExists("CARD", "Cardiologia", "Atendimento e tratamentos de doenças do coração.");
        createIfNotExists("DERM", "Dermatologia", "Cuidados com a pele, cabelos e unhas.");
        createIfNotExists("ORTO", "Ortopedia", "Diagnóstico e tratamento do sistema musculoesquelético.");
        createIfNotExists("PED",  "Pediatria", "Atendimento médico especializado em crianças.");
        createIfNotExists("GAST", "Gastroenterologia", "Tratamento de doenças do sistema digestório.");
        createIfNotExists("NEURO", "Neurologia", "Cuidados com o sistema nervoso central e periférico.");
        createIfNotExists("OBG",  "Obstetrícia", "Acompanhamento pré-natal e parto.");
        createIfNotExists("PSIQ", "Psiquiatria", "Diagnóstico e tratamento de transtornos mentais.");
        createIfNotExists("ONCO", "Oncologia", "Tratamento e acompanhamento de pacientes com câncer.");
        createIfNotExists("ENDO", "Endocrinologia", "Tratamento de distúrbios hormonais e metabólicos.");
    }

    private void createIfNotExists(String sigla, String nome, String descricao) {
        if (departmentService.findBySigla(sigla).isEmpty()) {
            CreateDepartmentRequest req = new CreateDepartmentRequest();
            req.setSigla(sigla);
            req.setNome(nome);
            req.setDescricao(descricao);
            Department dept = departmentService.create(req);
        }
    }
}
