package com.LETI_SIDIS_3DA_2.clinical_records_service.service;

import com.LETI_SIDIS_3DA_2.clinical_records_service.dto.CreateConsultaRegistoDTO;
import com.LETI_SIDIS_3DA_2.clinical_records_service.dto.ConsultaRegistoOutputDTO;
import java.util.Optional;

public interface ConsultaRegistoService {
    ConsultaRegistoOutputDTO createRecord(CreateConsultaRegistoDTO dto);
    Optional<ConsultaRegistoOutputDTO> getRecordByConsultaId(Long consultaId);
    // Outros métodos (ex: getRecordById, updateRecord)
}