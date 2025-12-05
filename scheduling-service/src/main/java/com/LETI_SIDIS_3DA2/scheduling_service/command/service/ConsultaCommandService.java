package com.LETI_SIDIS_3DA2.scheduling_service.command.service;

import com.LETI_SIDIS_3DA2.scheduling_service.command.dto.ConsultaInputDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.command.dto.UpdateConsultaDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.query.dto.ConsultaOutPutDTO;

import java.util.List;

public interface ConsultaCommandService {

    ConsultaOutPutDTO create(ConsultaInputDTO dto, String patientUsernameIgnored);

    ConsultaOutPutDTO cancel(Long id, String requesterUsername, List<String> requesterRoles);

    ConsultaOutPutDTO update(Long id, UpdateConsultaDTO dto,
                             String requesterUsername, List<String> requesterRoles);
}
