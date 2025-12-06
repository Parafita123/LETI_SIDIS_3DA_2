package com.LETI_SIDIS_3DA_2.clinical_records_service.command.controller;

import com.LETI_SIDIS_3DA_2.clinical_records_service.command.service.ConsultaRegistoCommandService;
import com.LETI_SIDIS_3DA_2.clinical_records_service.query.dto.ConsultaRegistoOutputDTO;
import com.LETI_SIDIS_3DA_2.clinical_records_service.command.dto.CreateConsultaRegistoDTO;
import com.LETI_SIDIS_3DA_2.clinical_records_service.exception.DuplicateResourceException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/records")
public class ConsultaRegistoCommandController {

    private final ConsultaRegistoCommandService commandService;

    public ConsultaRegistoCommandController(ConsultaRegistoCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PHYSICIAN')")
    public ResponseEntity<ConsultaRegistoOutputDTO> createRecord(
            @Valid @RequestBody CreateConsultaRegistoDTO dto) {

        try {
            ConsultaRegistoOutputDTO created = commandService.createRecord(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (DuplicateResourceException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
