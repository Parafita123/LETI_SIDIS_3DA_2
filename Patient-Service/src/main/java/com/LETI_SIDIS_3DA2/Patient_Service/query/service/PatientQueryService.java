package com.LETI_SIDIS_3DA2.Patient_Service.query.service;

import com.LETI_SIDIS_3DA2.Patient_Service.query.dto.PatientDto;

import java.util.List;

public interface PatientQueryService {

    PatientDto findById(Long id);

    List<PatientDto> searchByName(String name);
}