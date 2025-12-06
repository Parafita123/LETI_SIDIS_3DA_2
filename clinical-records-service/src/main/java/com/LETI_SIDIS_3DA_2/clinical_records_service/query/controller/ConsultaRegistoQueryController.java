package com.LETI_SIDIS_3DA_2.clinical_records_service.query.controller;

import com.LETI_SIDIS_3DA_2.clinical_records_service.query.dto.ConsultaRegistoOutputDTO;
import com.LETI_SIDIS_3DA_2.clinical_records_service.exception.ResourceNotFoundException;
import com.LETI_SIDIS_3DA_2.clinical_records_service.query.service.ConsultaRegistoQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/records")
public class ConsultaRegistoQueryController {

    private final ConsultaRegistoQueryService queryService;

    public ConsultaRegistoQueryController(ConsultaRegistoQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/by-appointment/{consultaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConsultaRegistoOutputDTO> getRecordByConsultaId(@PathVariable Long consultaId) {
        return queryService.getRecordByConsultaId(consultaId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registo não encontrado para a consulta com ID: " + consultaId
                ));
    }
}
