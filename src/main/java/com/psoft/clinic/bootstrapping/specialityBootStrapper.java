package com.psoft.clinic.bootstrapping;

import com.psoft.clinic.speciality.services.CreateSpecialityRequest;
import com.psoft.clinic.speciality.model.Speciality;
import com.psoft.clinic.speciality.services.SpecialityService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("bootstrap")
@RequiredArgsConstructor
@Order(2)
public class specialityBootStrapper implements CommandLineRunner {

    private final SpecialityService specialityService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        createIfNotExists("Cardiologia",       "CARDIO",   "Doenças e disfunções do coração");
        createIfNotExists("Dermatologia",      "DERMA",    "Cuidados com pele, cabelos e unhas");
        createIfNotExists("Ortopedia",         "ORTO",     "Sistema musculoesquelético");
        createIfNotExists("Pediatria",         "PED",      "Saúde infantil e adolescente");
        createIfNotExists("Gastroenterologia", "GASTRO",   "Trato digestório e órgãos associados");
        createIfNotExists("Neurologia",        "NEURO",    "Sistema nervoso central e periférico");
        createIfNotExists("Obstetrícia",       "OBST",     "Gravidez, parto e puerpério");
        createIfNotExists("Psiquiatria",       "PSIQ",     "Transtornos mentais e comportamentais");
        createIfNotExists("Oncologia",         "ONCO",     "Diagnóstico e tratamento do câncer");
        createIfNotExists("Endocrinologia",    "ENDO",     "Sistema endócrino e distúrbios hormonais");
    }

    private void createIfNotExists(String name, String acronym, String description) {
        if (specialityService.findByName(name).isEmpty()) {
            CreateSpecialityRequest req = new CreateSpecialityRequest();
            req.setName(name);
            req.setAcronym(acronym);
            req.setDescription(description);
            Speciality s = specialityService.create(req);
        }
    }
}
