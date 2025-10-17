package com.psoft2024._5.grupo1.projeto_psoft.controller;

import com.psoft2024._5.grupo1.projeto_psoft.domain.*;
import com.psoft2024._5.grupo1.projeto_psoft.dto.*;
import com.psoft2024._5.grupo1.projeto_psoft.service.*;
import com.psoft2024._5.grupo1.projeto_psoft.repository.*;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/registo")
public class ConsultaRegistoController {

    private final ConsultaRegistoServiceIntf service;
    private final ConsultasRepository consultaRepo;

    public ConsultaRegistoController(ConsultaRegistoServiceIntf service, ConsultasRepository consultaRepo) {
        this.service = service;
        this.consultaRepo = consultaRepo;
    }

    // 14 - Physician regista diagnóstico/tratamento/prescrições
    @PostMapping
    public ResponseEntity<EntityModel<ConsultasRegistoOutPutDTO>> create(
            @Valid @RequestBody ConsultasRegistoDTO dto) {

        ConsultasRegistoOutPutDTO created = service.criarRegisto(dto);

        EntityModel<ConsultasRegistoOutPutDTO> model = EntityModel.of(created,
                linkTo(methodOn(ConsultaRegistoController.class).getById(created.getId())).withSelfRel(),
                linkTo(methodOn(ConsultaRegistoController.class).getByConsultaId(created.getConsultaId())).withRel("consulta-registos")
        );

        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    // 17 - Admin ou paciente vê registo por ID
    @GetMapping("/{id}")
    public EntityModel<ConsultasRegistoOutPutDTO> getById(@PathVariable Long id) {
        ConsultasRegistoOutPutDTO dto = service.getById(id);
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registo com id " + id + " não encontrado.");
        }

        return EntityModel.of(dto,
                linkTo(methodOn(ConsultaRegistoController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(ConsultaRegistoController.class).getByConsultaId(dto.getConsultaId())).withRel("consulta-registos")
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PHYSICIAN')")
    public ResponseEntity<EntityModel<ConsultasRegistoOutPutDTO>> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody ConsultasRegistoDTO dto) {
        ConsultasRegistoOutPutDTO updated = service.updateRegisto(id, dto);
        if (updated == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registo com id " + id + " não encontrado.");
        }

        EntityModel<ConsultasRegistoOutPutDTO> model = EntityModel.of(updated,
                linkTo(methodOn(ConsultaRegistoController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(ConsultaRegistoController.class).getByConsultaId(updated.getConsultaId())).withRel("consulta-registos")
        );

        return ResponseEntity.ok(model);
    }

    @PostMapping("/{id}/prescription")
    @PreAuthorize("hasRole('PHYSICIAN')")
    public ResponseEntity<EntityModel<String>> generatePrescription(
            @PathVariable Long id) {
        String prescription = service.generatePrescription(id);
        if (prescription == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registo com id " + id + " não encontrado.");
        }

        EntityModel<String> model = EntityModel.of(prescription,
                linkTo(methodOn(ConsultaRegistoController.class).getById(id)).withRel("registo"));

        return ResponseEntity.ok(model);
    }

    // 15 ou 16 - Médico ou paciente vê registos de um paciente
    @GetMapping("/paciente/{patientId}")
    public CollectionModel<EntityModel<ConsultasRegistoOutPutDTO>> getByPaciente(@PathVariable Long patientId) {
        Patient p = new Patient();
        p.setId(patientId);
        List<Consulta> consultas = consultaRepo.findByPatient(p);
        if (consultas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Este paciente não tem consultas.");
        }

        List<EntityModel<ConsultasRegistoOutPutDTO>> registos = consultas.stream()
                .flatMap(c -> service.getRegistosByConsultaId(c.getId()).stream())
                .map(r -> EntityModel.of(r,
                        linkTo(methodOn(ConsultaRegistoController.class).getById(r.getId())).withSelfRel()))
                .collect(Collectors.toList());

        if (registos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Não existem registos para as consultas deste paciente.");
        }

        return CollectionModel.of(registos,
                linkTo(methodOn(ConsultaRegistoController.class).getByPaciente(patientId)).withSelfRel());
    }

    // Consultar todos os registos por consulta (se necessário)
    @GetMapping("/consulta/{consultaId}")
    public CollectionModel<EntityModel<ConsultasRegistoOutPutDTO>> getByConsultaId(@PathVariable Long consultaId) {
        List<ConsultasRegistoOutPutDTO> registos = service.getRegistosByConsultaId(consultaId);
        if (registos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Não existem registos para esta consulta.");
        }

        List<EntityModel<ConsultasRegistoOutPutDTO>> list = registos.stream()
                .map(r -> EntityModel.of(r,
                        linkTo(methodOn(ConsultaRegistoController.class).getById(r.getId())).withSelfRel()))
                .collect(Collectors.toList());

        return CollectionModel.of(list,
                linkTo(methodOn(ConsultaRegistoController.class).getByConsultaId(consultaId)).withSelfRel());
    }


}
