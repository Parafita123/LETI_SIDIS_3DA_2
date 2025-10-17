package com.psoft2024._5.grupo1.projeto_psoft.service;

import com.psoft2024._5.grupo1.projeto_psoft.dto.PatientCreateDto;
import com.psoft2024._5.grupo1.projeto_psoft.dto.PatientDto;
import com.psoft2024._5.grupo1.projeto_psoft.dto.PatientUpdateDto;

import java.util.List;

public interface PatientService {
    PatientDto register(PatientCreateDto in);
    PatientDto findById(Long id);
    List<PatientDto> searchByName(String name);
    PatientDto update(Long id, PatientUpdateDto in);
}
