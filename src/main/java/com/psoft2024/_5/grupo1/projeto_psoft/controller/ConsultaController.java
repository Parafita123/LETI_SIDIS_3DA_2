package com.psoft2024._5.grupo1.projeto_psoft.controller;


import com.psoft2024._5.grupo1.projeto_psoft.domain.*;
import com.psoft2024._5.grupo1.projeto_psoft.dto.*;
import com.psoft2024._5.grupo1.projeto_psoft.service.*;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.Map;
import com.psoft2024._5.grupo1.projeto_psoft.dto.AgeGroupStatsDto;
import org.springframework.security.access.prepost.PreAuthorize;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    private final ConsultaServiceIntf consultaService;
    public ConsultaController(ConsultaServiceIntf consultaService) {
        this.consultaService = consultaService;
    }
    @PostMapping
    public ResponseEntity<EntityModel<ConsultaOutPutDTO>> create(@Valid @RequestBody ConsultaDTO dto) {
        ConsultaOutPutDTO created = consultaService.create(dto);

        EntityModel<ConsultaOutPutDTO> model = EntityModel.of(created,
                linkTo(methodOn(ConsultaController.class).getById(created.getId())).withSelfRel(),
                linkTo(methodOn(ConsultaController.class).getMine(created.getId())).withRel("mine"),
                linkTo(methodOn(ConsultaController.class).update(created.getId(), created, false)).withRel("update"),
                linkTo(methodOn(ConsultaController.class).cancel(created.getId())).withRel("cancel")
        );

        return ResponseEntity
                .created(model.getRequiredLink("self").toUri())
                .body(model);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('PATIENT')")
    @GetMapping("/{id}")
    public EntityModel<ConsultaOutPutDTO> getById(@PathVariable Long id) {
        ConsultaOutPutDTO dto = consultaService.getById(id);

        return EntityModel.of(dto,
                linkTo(methodOn(ConsultaController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(ConsultaController.class).getMine(dto.getId())).withRel("mine")
        );
    }
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/mine")
    public CollectionModel<EntityModel<ConsultaOutPutDTO>> getMine(@RequestParam Long patientId) {
        Patient p = new Patient(); p.setId(patientId);

        List<EntityModel<ConsultaOutPutDTO>> list = consultaService.getByPatient(p).stream()
                .map(dto -> EntityModel.of(dto,
                        linkTo(methodOn(ConsultaController.class).getById(dto.getId())).withSelfRel()))
                .collect(Collectors.toList());

        return CollectionModel.of(list,
                linkTo(methodOn(ConsultaController.class).getMine(patientId)).withSelfRel());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('PATIENT')")
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<EntityModel<ConsultaOutPutDTO>> cancel(@PathVariable Long id) {
        ConsultaOutPutDTO dto = consultaService.cancel(id);

        EntityModel<ConsultaOutPutDTO> model = EntityModel.of(dto,
                linkTo(methodOn(ConsultaController.class).getById(id)).withSelfRel());

        return ResponseEntity.ok(model);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('PATIENT')")
    @PutMapping("update/{id}")
    public ResponseEntity<EntityModel<ConsultaOutPutDTO>> update(
            @PathVariable Long id,
            @RequestBody ConsultaOutPutDTO dto,
            @RequestParam(defaultValue = "false") boolean cancel
    ) {
        if (cancel) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Para cancelar, use o endpoint /{id}/cancelar");
        }

        ConsultaOutPutDTO updated = consultaService.update(id, dto);

        EntityModel<ConsultaOutPutDTO> model = EntityModel.of(updated,
                linkTo(methodOn(ConsultaController.class).getById(id)).withSelfRel());

        return ResponseEntity.ok(model);
    }
    // Consultas futuras ordenadas
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/proximas")
    public CollectionModel<EntityModel<ConsultaOutPutDTO>> getUpcoming() {
        List<EntityModel<ConsultaOutPutDTO>> list = consultaService.getUpcomingAppointments().stream()
                .map(dto -> EntityModel.of(dto,
                        linkTo(methodOn(ConsultaController.class).getById(dto.getId())).withSelfRel()))
                .toList();

        return CollectionModel.of(list, linkTo(methodOn(ConsultaController.class).getUpcoming()).withSelfRel());
    }

    // Duração média por médico
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/estatistica/media")
    public ResponseEntity<Map<String, Double>> getAverageDuration() {
        return ResponseEntity.ok(consultaService.getAverageDurationPerPhysician());
    }

    // Relatório mensal
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/estatistica/relatorio")
    public ResponseEntity<ConsultasReportDTO> getMonthlyReport(@RequestParam int year, @RequestParam int month) {
        if (month < 1 || month > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(consultaService.getMonthlyReport(year, month));
    }
    @GetMapping("/stats/age-groups")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AgeGroupStatsDto>> statsByAgeGroup() {
        List<AgeGroupStatsDto> stats = consultaService.getStatsByPatientAgeGroups();
        return ResponseEntity.ok(stats);
    }

}
