package com.LETI_SIDIS_3DA2.scheduling_service.service;

import com.LETI_SIDIS_3DA2.scheduling_service.command.dto.ConsultaInputDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.command.dto.UpdateConsultaDTO;
import com.LETI_SIDIS_3DA2.scheduling_service.query.dto.AgeGroupStatsDto;
import com.LETI_SIDIS_3DA2.scheduling_service.query.dto.ConsultaOutPutDTO;

import java.util.List;
import java.util.Map;

public interface ConsultaServiceIntf {

    // Aceita DTO de entrada e o username do paciente autenticado
    ConsultaOutPutDTO create(ConsultaInputDTO dto, String patientUsername);

    // Requer info do chamador para verificar permissões
    ConsultaOutPutDTO getById(Long id, String requesterUsername, List<String> requesterRoles);

    // Busca consultas pelo username do paciente, não pela entidade
    List<ConsultaOutPutDTO> getByPatientUsername(String patientUsername);

    // Requer info do chamador para verificar permissões
    ConsultaOutPutDTO cancel(Long id, String requesterUsername, List<String> requesterRoles);

    // Aceita DTO de entrada para atualização e info do chamador
    ConsultaOutPutDTO update(Long id, UpdateConsultaDTO dto, String requesterUsername, List<String> requesterRoles);

    // Relatórios (mantêm-se, mas a implementação interna mudará)
    List<ConsultaOutPutDTO> getUpcomingAppointments();
    Map<String, Double> getAverageDurationPerPhysician();
    List<AgeGroupStatsDto> getStatsByPatientAgeGroups();
    Map<String, Long> getMonthlyReport(int year, int month);
}