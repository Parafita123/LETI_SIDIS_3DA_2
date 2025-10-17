package com.psoft2024._5.grupo1.projeto_psoft.controller;

import com.psoft2024._5.grupo1.projeto_psoft.dto.PatientCreateDto;
import com.psoft2024._5.grupo1.projeto_psoft.dto.PatientDto;
import com.psoft2024._5.grupo1.projeto_psoft.service.PatientService;
import jakarta.validation.Valid;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.psoft2024._5.grupo1.projeto_psoft.dto.PatientUpdateDto;
import org.springframework.security.access.prepost.PreAuthorize;


import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService svc;
    public PatientController(PatientService svc) {
        this.svc = svc;

    }

    @PostMapping
    public ResponseEntity<EntityModel<PatientDto>> register(
            @Valid @RequestBody PatientCreateDto in) {
        PatientDto dto = svc.register(in);
        EntityModel<PatientDto> model = EntityModel.of(dto,
                linkTo(methodOn(PatientController.class).register(in)).withSelfRel(),
                linkTo(methodOn(PatientController.class).getById(dto.getId())).withRel("patient"),
                linkTo(methodOn(PatientController.class).search("")).withRel("patients")
        );
        return ResponseEntity
                .created(model.getRequiredLink("self").toUri())
                .body(model);
    }

    @GetMapping("/{id}")
    public EntityModel<PatientDto> getById(@PathVariable Long id) {
        PatientDto dto = svc.findById(id);
        return EntityModel.of(dto,
                linkTo(methodOn(PatientController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(PatientController.class).search("")).withRel("patients")
        );
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('PATIENT')")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<PatientDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody PatientUpdateDto in
    ) {
        PatientDto updated = svc.update(id, in);
        EntityModel<PatientDto> model = EntityModel.of(updated,
                linkTo(methodOn(PatientController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(PatientController.class).search("")).withRel("patients")
        );
        return ResponseEntity.ok(model);
    }

    @GetMapping
    public CollectionModel<EntityModel<PatientDto>> search(
            @RequestParam(name = "name", required = true) String name) {

        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O parâmetro 'name' é obrigatório e não pode estar vazio"
            );
        }

        List<EntityModel<PatientDto>> all = svc.searchByName(name.trim()).stream()
                .map(dto -> EntityModel.of(dto,
                        linkTo(methodOn(PatientController.class).getById(dto.getId())).withSelfRel()))
                .toList();

        return CollectionModel.of(all,
                linkTo(methodOn(PatientController.class).search(name.trim())).withSelfRel());
    }
}
