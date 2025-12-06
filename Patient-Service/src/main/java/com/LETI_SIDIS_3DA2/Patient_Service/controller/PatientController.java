package com.LETI_SIDIS_3DA2.Patient_Service.controller;

import com.LETI_SIDIS_3DA2.Patient_Service.command.dto.PatientCreateDto;
import com.LETI_SIDIS_3DA2.Patient_Service.query.dto.PatientDto;
import com.LETI_SIDIS_3DA2.Patient_Service.command.dto.PatientUpdateDto;
import com.LETI_SIDIS_3DA2.Patient_Service.command.service.PatientCommandService;
import com.LETI_SIDIS_3DA2.Patient_Service.query.service.PatientQueryService;
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

    private final PatientCommandService commandService;
    private final PatientQueryService queryService;

    public PatientController(PatientCommandService commandService,
                             PatientQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    // endpoint que CRIA paciente -> comando
    @PostMapping
    public ResponseEntity<PatientDto> register(@Valid @RequestBody PatientCreateDto in) {
        PatientDto created = commandService.register(in);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // endpoint que ATUALIZA paciente -> comando
    @PutMapping("/{id}")
    public ResponseEntity<PatientDto> update(@PathVariable Long id,
                                             @Valid @RequestBody PatientUpdateDto in) {
        PatientDto updated = commandService.update(id, in);
        return ResponseEntity.ok(updated);
    }

    // endpoints só de LEITURA -> query

    @GetMapping("/{id}")
    public ResponseEntity<PatientDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(queryService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PatientDto>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(queryService.searchByName(name));
    }
}
