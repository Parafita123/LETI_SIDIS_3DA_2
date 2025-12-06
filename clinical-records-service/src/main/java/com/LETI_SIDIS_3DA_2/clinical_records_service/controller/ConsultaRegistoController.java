package com.LETI_SIDIS_3DA_2.clinical_records_service.controller;

import com.LETI_SIDIS_3DA_2.clinical_records_service.command.dto.CreateConsultaRegistoDTO;
import com.LETI_SIDIS_3DA_2.clinical_records_service.query.dto.ConsultaRegistoOutputDTO;

// Exceções
import com.LETI_SIDIS_3DA_2.clinical_records_service.exception.DuplicateResourceException;
import com.LETI_SIDIS_3DA_2.clinical_records_service.exception.ResourceNotFoundException;

// Serviços
import com.LETI_SIDIS_3DA_2.clinical_records_service.service.ConsultaRegistoService;

// Validação
import jakarta.validation.Valid;

// Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/records") // Ou um path mais descritivo
public class ConsultaRegistoController {

    private final ConsultaRegistoService recordService;

    @Autowired
    public ConsultaRegistoController(ConsultaRegistoService recordService) {
        this.recordService = recordService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PHYSICIAN')") // Apenas médicos podem criar registos
    public ResponseEntity<ConsultaRegistoOutputDTO> createRecord(@Valid @RequestBody CreateConsultaRegistoDTO dto) {
        try {
            ConsultaRegistoOutputDTO createdDto = recordService.createRecord(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDto);
        } catch (DuplicateResourceException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/by-appointment/{consultaId}")
    @PreAuthorize("isAuthenticated()") // Qualquer utilizador autenticado pode ver (a lógica de quem pode ver qual registo seria mais fina)
    public ResponseEntity<ConsultaRegistoOutputDTO> getRecordByConsultaId(@PathVariable Long consultaId) {
        return recordService.getRecordByConsultaId(consultaId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Registo não encontrado para a consulta com ID: " + consultaId));
    }
}