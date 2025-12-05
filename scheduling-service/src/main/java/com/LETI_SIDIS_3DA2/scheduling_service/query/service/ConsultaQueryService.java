package com.LETI_SIDIS_3DA2.scheduling_service.query.service;

import com.LETI_SIDIS_3DA2.scheduling_service.query.dto.AgeGroupStatsDto;
import com.LETI_SIDIS_3DA2.scheduling_service.query.dto.ConsultaOutPutDTO;

import java.util.List;
import java.util.Map;

public interface ConsultaQueryService {

    ConsultaOutPutDTO getById(Long id, String requesterUsername, List<String> requesterRoles);

    List<ConsultaOutPutDTO> getByPatientUsername(String patientUsername);

    List<ConsultaOutPutDTO> getUpcomingAppointments();

    Map<String, Double> getAverageDurationPerPhysician();

    Map<String, Long> getMonthlyReport(int year, int month);

    List<AgeGroupStatsDto> getStatsByPatientAgeGroups();
}
