package com.LETI_SIDIS_3DA2.Patient_Service.controller;

import com.LETI_SIDIS_3DA2.Patient_Service.dto.PatientCreateDto;
import com.LETI_SIDIS_3DA2.Patient_Service.dto.PatientDto;
import com.LETI_SIDIS_3DA2.Patient_Service.dto.PatientUpdateDto;
import com.LETI_SIDIS_3DA2.Patient_Service.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService svc;
    public PatientController(PatientService svc) {
        this.svc = svc;

    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
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
