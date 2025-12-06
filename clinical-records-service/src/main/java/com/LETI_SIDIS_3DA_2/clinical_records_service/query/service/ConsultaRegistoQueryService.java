package com.LETI_SIDIS_3DA_2.clinical_records_service.query.service;

import com.LETI_SIDIS_3DA_2.clinical_records_service.query.dto.ConsultaRegistoOutputDTO;

import java.util.Optional;

public interface ConsultaRegistoQueryService {

    Optional<ConsultaRegistoOutputDTO> getRecordByConsultaId(Long consultaId);
}
