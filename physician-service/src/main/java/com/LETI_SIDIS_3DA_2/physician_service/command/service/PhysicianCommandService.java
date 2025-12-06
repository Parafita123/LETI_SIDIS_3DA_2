package com.LETI_SIDIS_3DA_2.physician_service.command.service;

import com.LETI_SIDIS_3DA_2.physician_service.command.dto.RegisterPhysicianDTO;
import com.LETI_SIDIS_3DA_2.physician_service.query.dto.PhysicianOutputDTO;
import org.springframework.web.multipart.MultipartFile;

public interface PhysicianCommandService {

    PhysicianOutputDTO registerPhysician(RegisterPhysicianDTO physicianDTO,
                                         MultipartFile profilePhotoFile);

    PhysicianOutputDTO updatePhysician(Long id, RegisterPhysicianDTO physicianDTO);
}
