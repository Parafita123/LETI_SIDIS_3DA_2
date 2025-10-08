package com.psoft2024._5.grupo1.projeto_psoft.service;


import com.psoft2024._5.grupo1.projeto_psoft.domain.Patient;
import com.psoft2024._5.grupo1.projeto_psoft.dto.*;
import java.util.Map;
import java.util.List;
import com.psoft2024._5.grupo1.projeto_psoft.dto.AgeGroupStatsDto;

public interface ConsultaServiceIntf {

    ConsultaOutPutDTO create(ConsultaDTO dto);

    List<ConsultaOutPutDTO> getByPatient(Patient patient);

    ConsultaOutPutDTO getById(Long id);

    ConsultaOutPutDTO cancel(Long id);

    ConsultaOutPutDTO update(Long id, ConsultaOutPutDTO  dto);

    List<ConsultaOutPutDTO> getUpcomingAppointments();

    Map<String, Double> getAverageDurationPerPhysician();

    ConsultasReportDTO getMonthlyReport(int year, int month);

    List<AgeGroupStatsDto> getStatsByPatientAgeGroups();
}
