package com.LETI_SIDIS_3DA2.scheduling_service.controller;

import com.LETI_SIDIS_3DA2.scheduling_service.dto.*;
import com.LETI_SIDIS_3DA2.scheduling_service.service.ConsultaServiceIntf;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    private final ConsultaServiceIntf consultaService;

    @Autowired
    public ConsultaController(ConsultaServiceIntf consultaService) {
        this.consultaService = consultaService;
    }

    @PostMapping
    public ResponseEntity<ConsultaOutPutDTO> create(@Valid @RequestBody ConsultaInputDTO dto,
                                                    Authentication authentication) {
        String authenticatedUsername = authentication != null ? authentication.getName() : null;
        try {
            ConsultaOutPutDTO created = consultaService.create(dto, authenticatedUsername);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultaOutPutDTO> getById(@PathVariable Long id, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        // Se quiseres: List<String> roles = authentication == null ? List.of() : authentication.getAuthorities().stream().map(Object::toString).toList();
        List<String> roles = authentication == null ? List.of() : authentication.getAuthorities().stream().map(Object::toString).toList();
        ConsultaOutPutDTO dto = consultaService.getById(id, username, roles);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ConsultaOutPutDTO>> getMine(Authentication authentication) {
        String authenticatedUsername = authentication != null ? authentication.getName() : null;
        List<ConsultaOutPutDTO> list = consultaService.getByPatientUsername(authenticatedUsername);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ConsultaOutPutDTO> cancel(@PathVariable Long id, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        List<String> roles = authentication == null ? List.of() : authentication.getAuthorities().stream().map(Object::toString).toList();
        ConsultaOutPutDTO dto = consultaService.cancel(id, username, roles);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsultaOutPutDTO> update(@PathVariable Long id,
                                                    @Valid @RequestBody UpdateConsultaDTO dto,
                                                    Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        List<String> roles = authentication == null ? List.of() : authentication.getAuthorities().stream().map(Object::toString).toList();
        ConsultaOutPutDTO updated = consultaService.update(id, dto, username, roles);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/proximas")
    public ResponseEntity<List<ConsultaOutPutDTO>> getUpcoming() {
        List<ConsultaOutPutDTO> list = consultaService.getUpcomingAppointments();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/estatistica/duracao-media")
    public ResponseEntity<Map<String, Double>> getAverageDuration() {
        return ResponseEntity.ok(consultaService.getAverageDurationPerPhysician());
    }

    @GetMapping("/estatistica/relatorio-mensal")
    public ResponseEntity<Map<String, Long>> getMonthlyReport(@RequestParam int year, @RequestParam int month) {
        if (month < 1 || month > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mês inválido.");
        }
        return ResponseEntity.ok(consultaService.getMonthlyReport(year, month));
    }

    @GetMapping("/estatistica/grupos-idade")
    public ResponseEntity<List<AgeGroupStatsDto>> getStatsByAgeGroup() {
        return ResponseEntity.ok(consultaService.getStatsByPatientAgeGroups());
    }
}
