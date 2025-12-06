package com.LETI_SIDIS_3DA_2.physician_service.query.service;

import com.LETI_SIDIS_3DA_2.physician_service.query.dto.PhysicianBasicInfoDTO;
import com.LETI_SIDIS_3DA_2.physician_service.query.dto.PhysicianOutputDTO;

import java.util.List;
import java.util.Optional;

public interface PhysicianQueryService {

    Optional<PhysicianOutputDTO> getPhysicianById(Long id);

    Optional<PhysicianBasicInfoDTO> getPhysicianBasicInfoById(Long id);

    // pesquisa “completa” (ex: para admins / backend)
    List<PhysicianOutputDTO> searchPhysicians(String name, String specialtyName);

    // pesquisa simplificada para pacientes
    List<PhysicianBasicInfoDTO> searchPhysiciansForPatient(String name, String specialtyName);
}
