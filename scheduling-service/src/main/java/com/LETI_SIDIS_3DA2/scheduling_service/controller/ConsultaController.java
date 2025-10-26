package com.LETI_SIDIS_3DA2.scheduling_service.controller;

import com.LETI_SIDIS_3DA2.scheduling_service.dto.*; // Assume que tens os DTOs necessários aqui
import com.LETI_SIDIS_3DA2.scheduling_service.service.ConsultaServiceIntf;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // Para obter o utilizador autenticado
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultas") // Mudei para "consultas" para consistência
public class ConsultaController {

    private final ConsultaServiceIntf consultaService;

    @Autowired
    public ConsultaController(ConsultaServiceIntf consultaService) {
        this.consultaService = consultaService;
    }

    // UC: Marcar uma nova consulta (ex: POST /api/consultas)
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')") // Apenas um paciente pode marcar uma consulta para si
    public ResponseEntity<ConsultaOutPutDTO> create(@Valid @RequestBody ConsultaInputDTO dto, Authentication authentication) {
        // O ID do paciente virá do token JWT do utilizador autenticado
        // Precisamos de uma forma de mapear o username (do token) para o patientId.
        // Isto exigirá uma chamada ao Patient Service. Vamos adicionar esta lógica no serviço.
        String authenticatedUsername = authentication.getName();

        try {
            ConsultaOutPutDTO created = consultaService.create(dto, authenticatedUsername);
            // Links HATEOAS podem ser adicionados aqui
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) { // Captura exceções como "Slot not available", "Physician not found", etc.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // UC: Ver detalhes de uma consulta (ex: GET /api/consultas/{id})
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Qualquer utilizador autenticado pode tentar, a lógica de negócio no serviço valida a permissão
    public ResponseEntity<ConsultaOutPutDTO> getById(@PathVariable Long id, Authentication authentication) {
        // Passa o nome de utilizador e as suas roles para o serviço validar se ele pode ver esta consulta
        String username = authentication.getName();
        List<String> roles = authentication.getAuthorities().stream().map(Object::toString).toList();

        ConsultaOutPutDTO dto = consultaService.getById(id, username, roles);
        // Links HATEOAS
        return ResponseEntity.ok(dto);
    }

    // UC: Ver as minhas próprias consultas (exclusivo para Pacientes)
    // GET /api/consultas/mine
    @GetMapping("/mine")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<ConsultaOutPutDTO>> getMine(Authentication authentication) {
        String authenticatedUsername = authentication.getName();
        List<ConsultaOutPutDTO> list = consultaService.getByPatientUsername(authenticatedUsername);
        // Links HATEOAS
        return ResponseEntity.ok(list);
    }

    // UC: Cancelar uma consulta
    @PutMapping("/{id}/cancelar")
    @PreAuthorize("isAuthenticated()") // A validação de quem pode cancelar (o próprio paciente ou um admin) será feita no serviço
    public ResponseEntity<ConsultaOutPutDTO> cancel(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        List<String> roles = authentication.getAuthorities().stream().map(Object::toString).toList();

        ConsultaOutPutDTO dto = consultaService.cancel(id, username, roles);
        // Links HATEOAS
        return ResponseEntity.ok(dto);
    }

    // UC: Atualizar uma consulta (reagendar) - Deve usar um DTO de entrada específico
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PATIENT')") // Paciente ou Admin podem reagendar
    public ResponseEntity<ConsultaOutPutDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateConsultaDTO dto, // Usa um DTO de entrada para atualização
            Authentication authentication
    ) {
        String username = authentication.getName();
        List<String> roles = authentication.getAuthorities().stream().map(Object::toString).toList();

        ConsultaOutPutDTO updated = consultaService.update(id, dto, username, roles);
        // Links HATEOAS
        return ResponseEntity.ok(updated);
    }

    // --- Endpoints de Relatórios (Apenas Admin) ---

    @GetMapping("/proximas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ConsultaOutPutDTO>> getUpcoming() {
        List<ConsultaOutPutDTO> list = consultaService.getUpcomingAppointments();
        // Links HATEOAS
        return ResponseEntity.ok(list);
    }

    @GetMapping("/estatistica/duracao-media")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Double>> getAverageDuration() {
        // O serviço agora terá de chamar o Physician Service para obter os nomes dos médicos
        return ResponseEntity.ok(consultaService.getAverageDurationPerPhysician());
    }

    @GetMapping("/estatistica/relatorio-mensal")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getMonthlyReport(@RequestParam int year, @RequestParam int month) {
        if (month < 1 || month > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mês inválido.");
        }
        return ResponseEntity.ok(consultaService.getMonthlyReport(year, month));
    }

    @GetMapping("/estatistica/grupos-idade")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AgeGroupStatsDto>> getStatsByAgeGroup() {
        // Este método precisará de chamar o Patient Service para obter as idades dos pacientes
        return ResponseEntity.ok(consultaService.getStatsByPatientAgeGroups());
    }
}