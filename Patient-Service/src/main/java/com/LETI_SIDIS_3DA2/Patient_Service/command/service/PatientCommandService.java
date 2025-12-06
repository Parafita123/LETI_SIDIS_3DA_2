package com.LETI_SIDIS_3DA2.Patient_Service.command.service;

import com.LETI_SIDIS_3DA2.Patient_Service.command.dto.PatientCreateDto;
import com.LETI_SIDIS_3DA2.Patient_Service.command.dto.PatientUpdateDto;
import com.LETI_SIDIS_3DA2.Patient_Service.query.dto.PatientDto;

public interface PatientCommandService {

    PatientDto register(PatientCreateDto in);

    PatientDto update(Long id, PatientUpdateDto in);
}