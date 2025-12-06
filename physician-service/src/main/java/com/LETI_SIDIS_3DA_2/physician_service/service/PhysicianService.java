/*package com.LETI_SIDIS_3DA_2.physician_service.service;

import com.LETI_SIDIS_3DA_2.physician_service.command.dto.RegisterPhysicianDTO;
import com.LETI_SIDIS_3DA_2.physician_service.query.dto.PhysicianBasicInfoDTO;
import com.LETI_SIDIS_3DA_2.physician_service.query.dto.PhysicianOutputDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface PhysicianService {
    PhysicianOutputDTO registerPhysician(RegisterPhysicianDTO physicianDTO, MultipartFile profilePhoto);
    Optional<PhysicianOutputDTO> getPhysicianById(Long id);
    List<PhysicianOutputDTO> searchPhysicians(String name, String specialtyName);
    Optional<PhysicianBasicInfoDTO> getPhysicianBasicInfoById(Long id);
    List<PhysicianBasicInfoDTO> searchPhysiciansForPatient(String name, String specialtyName);
    PhysicianOutputDTO updatePhysician(Long id, RegisterPhysicianDTO physicianDTO);
}
*/
