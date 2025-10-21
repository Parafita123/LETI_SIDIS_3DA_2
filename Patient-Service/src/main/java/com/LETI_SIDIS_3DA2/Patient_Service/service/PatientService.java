package com.LETI_SIDIS_3DA2.Patient_Service.service;

import com.LETI_SIDIS_3DA2.Patient_Service.dto.PatientCreateDto;
import com.LETI_SIDIS_3DA2.Patient_Service.dto.PatientDto;
import com.LETI_SIDIS_3DA2.Patient_Service.dto.PatientUpdateDto;

import java.util.List;

public interface PatientService {
    PatientDto register(PatientCreateDto in);
    PatientDto findById(Long id);
    List<PatientDto> searchByName(String name);
    PatientDto update(Long id, PatientUpdateDto in);
}
