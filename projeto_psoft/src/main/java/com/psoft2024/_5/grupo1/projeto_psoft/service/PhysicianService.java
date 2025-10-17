package com.psoft2024._5.grupo1.projeto_psoft.service;

import com.psoft2024._5.grupo1.projeto_psoft.dto.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface PhysicianService {
    PhysicianOutputDTO registerPhysician(RegisterPhysicianDTO physicianDTO, MultipartFile profilePhoto);
    Optional<PhysicianOutputDTO> getPhysicianById(Long id);
    List<PhysicianOutputDTO> searchPhysicians(String name, String specialtyName);
    Optional<PhysicianBasicInfoDTO> getPhysicianBasicInfoById(Long id);
    List<PhysicianBasicInfoDTO> searchPhysiciansForPatient(String name, String specialtyName);
    PhysicianOutputDTO updatePhysician(Long id, RegisterPhysicianDTO physicianDTO);
    List<PhysicianAppointmentCountDTO> getTop5PhysiciansByAppointments();
    List<AvailableSlotDTO> getAvailableSlots(Long physicianId, LocalDate startDate);
}
