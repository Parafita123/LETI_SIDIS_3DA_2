package com.LETI_SIDIS_3DA_2.clinical_records_service.command.service;

import com.LETI_SIDIS_3DA_2.clinical_records_service.query.dto.ConsultaRegistoOutputDTO;
import com.LETI_SIDIS_3DA_2.clinical_records_service.command.dto.CreateConsultaRegistoDTO;

public interface ConsultaRegistoCommandService {

    ConsultaRegistoOutputDTO createRecord(CreateConsultaRegistoDTO dto);
}
