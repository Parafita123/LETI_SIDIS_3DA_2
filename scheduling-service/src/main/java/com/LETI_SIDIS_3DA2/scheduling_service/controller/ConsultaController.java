package com.LETI_SIDIS_3DA2.scheduling_service.controller;

import com.LETI_SIDIS_3DA2.scheduling_service.command.dto.ConsultaInputDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.command.dto.UpdateConsultaDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.command.service.ConsultaCommandService;
import com.LETI_SIDIS_3DA2.scheduling_service.query.dto.AgeGroupStatsDto;
import com.LETI_SIDIS_3DA2.scheduling_service.query.dto.ConsultaOutPutDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.query.service.ConsultaQueryService;
import jakarta.validation.Valid;
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

    private final ConsultaCommandService commandService;
    private final ConsultaQueryService queryService;

    public ConsultaController(ConsultaCommandService commandService,
                              ConsultaQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<ConsultaOutPutDTO> create(@Valid @RequestBody ConsultaInputDTO dto,
                                                    Authentication authentication) {
        String authenticatedUsername = authentication != null ? authentication.getName() : null;
        try {
            ConsultaOutPutDTO created = commandService.create(dto, authenticatedUsername);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultaOutPutDTO> getById(@PathVariable Long id, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        List<String> roles = authentication == null ? List.of()
                : authentication.getAuthorities().stream().map(Object::toString).toList();
        ConsultaOutPutDTO dto = queryService.getById(id, username, roles);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ConsultaOutPutDTO>> getMine(Authentication authentication) {
        String authenticatedUsername = authentication != null ? authentication.getName() : null;
        List<ConsultaOutPutDTO> list = queryService.getByPatientUsername(authenticatedUsername);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ConsultaOutPutDTO> cancel(@PathVariable Long id, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        List<String> roles = authentication == null ? List.of()
                : authentication.getAuthorities().stream().map(Object::toString).toList();
        ConsultaOutPutDTO dto = commandService.cancel(id, username, roles);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsultaOutPutDTO> update(@PathVariable Long id,
                                                    @Valid @RequestBody UpdateConsultaDTO dto,
                                                    Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        List<String> roles = authentication == null ? List.of()
                : authentication.getAuthorities().stream().map(Object::toString).toList();
        ConsultaOutPutDTO updated = commandService.update(id, dto, username, roles);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/proximas")
    public ResponseEntity<List<ConsultaOutPutDTO>> getUpcoming() {
        return ResponseEntity.ok(queryService.getUpcomingAppointments());
    }

    @GetMapping("/estatistica/duracao-media")
    public ResponseEntity<Map<String, Double>> getAverageDuration() {
        return ResponseEntity.ok(queryService.getAverageDurationPerPhysician());
    }

    @GetMapping("/estatistica/relatorio-mensal")
    public ResponseEntity<Map<String, Long>> getMonthlyReport(@RequestParam int year, @RequestParam int month) {
        if (month < 1 || month > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mês inválido.");
        }
        return ResponseEntity.ok(queryService.getMonthlyReport(year, month));
    }

    @GetMapping("/estatistica/grupos-idade")
    public ResponseEntity<List<AgeGroupStatsDto>> getStatsByAgeGroup() {
        return ResponseEntity.ok(queryService.getStatsByPatientAgeGroups());
    }
}
