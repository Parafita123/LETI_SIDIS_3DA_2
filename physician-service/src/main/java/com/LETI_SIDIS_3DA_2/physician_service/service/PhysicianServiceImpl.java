package com.LETI_SIDIS_3DA_2.physician_service.service;

import com.LETI_SIDIS_3DA_2.physician_service.domain.Department;
import com.LETI_SIDIS_3DA_2.physician_service.domain.Physician;
import com.LETI_SIDIS_3DA_2.physician_service.domain.Specialization;
import com.LETI_SIDIS_3DA_2.physician_service.dto.PhysicianBasicInfoDTO;
import com.LETI_SIDIS_3DA_2.physician_service.dto.PhysicianOutputDTO;
import com.LETI_SIDIS_3DA_2.physician_service.dto.RegisterPhysicianDTO;
import com.LETI_SIDIS_3DA_2.physician_service.exception.DuplicateResourceException;
import com.LETI_SIDIS_3DA_2.physician_service.exception.ResourceNotFoundException;
import com.LETI_SIDIS_3DA_2.physician_service.repository.DepartmentRepository;
import com.LETI_SIDIS_3DA_2.physician_service.repository.PhysicianRepository;
import com.LETI_SIDIS_3DA_2.physician_service.repository.SpecializationRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PhysicianServiceImpl implements PhysicianService {

    private final PhysicianRepository physicianRepository;
    private final SpecializationRepository specializationRepository;
    private final DepartmentRepository departmentRepository;
    private final FileStorageService fileStorageService;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    public PhysicianServiceImpl(PhysicianRepository physicianRepository,
                                SpecializationRepository specializationRepository,
                                DepartmentRepository departmentRepository,
                                FileStorageService fileStorageService) {
        this.physicianRepository = physicianRepository;
        this.specializationRepository = specializationRepository;
        this.departmentRepository = departmentRepository;
        this.fileStorageService = fileStorageService;
    }

    private PhysicianOutputDTO convertToDTO(Physician physician) {
        if (physician == null) {
            return null;
        }
        String photoUrl = null;
        if (physician.getProfilePhotoPath() != null && !physician.getProfilePhotoPath().isEmpty()) {
            photoUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .pathSegment("api", "physicians", "photo", physician.getProfilePhotoPath())
                    .toUriString();
        }
        return new PhysicianOutputDTO(
                physician.getId(),
                physician.getFullName(),
                physician.getSpecialty() != null ? physician.getSpecialty().getName() : null,
                physician.getDepartment() != null ? physician.getDepartment().getAcronym() : null,
                physician.getEmail(),
                physician.getPhoneNumber(),
                physician.getAddress(),
                physician.getWorkStartTime(),
                physician.getWorkEndTime(),
                physician.getOptionalDescription(),
                photoUrl
        );
    }

    private PhysicianBasicInfoDTO convertToBasicDTO(Physician physician) {
        if (physician == null) {
            return null;
        }
        return new PhysicianBasicInfoDTO(
                physician.getFullName(),
                physician.getSpecialty() != null ? physician.getSpecialty().getName() : null
        );
    }

    @Override
    @Transactional
    public PhysicianOutputDTO registerPhysician(RegisterPhysicianDTO physicianDTO, MultipartFile profilePhotoFile) {
        if (physicianRepository.findByEmail(physicianDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email já existe: " + physicianDTO.getEmail());
        }
        if (physicianRepository.findByPhoneNumber(physicianDTO.getPhoneNumber()).isPresent()) {
            throw new DuplicateResourceException("Número de telefone já existe: " + physicianDTO.getPhoneNumber());
        }

        Specialization specialty = specializationRepository.findById(physicianDTO.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with ID: " + physicianDTO.getSpecialtyId()));
        Department department = departmentRepository.findById(physicianDTO.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + physicianDTO.getDepartmentId()));

        LocalTime startTime;
        LocalTime endTime;
        try {
            startTime = LocalTime.parse(physicianDTO.getWorkStartTime(), timeFormatter);
            endTime = LocalTime.parse(physicianDTO.getWorkEndTime(), timeFormatter);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid time format for working hours. Use HH:mm. Details: " + e.getMessage(), e);
        }

        String profilePhotoFileName = fileStorageService.storePhysicianProfilePhoto(profilePhotoFile);

        Physician physician = new Physician(
                physicianDTO.getFullName(),
                specialty,
                department,
                physicianDTO.getEmail(),
                physicianDTO.getPhoneNumber(),
                physicianDTO.getAddress(),
                startTime,
                endTime,
                physicianDTO.getOptionalDescription()
        );

        if (profilePhotoFileName != null) {
            physician.setProfilePhotoPath(profilePhotoFileName);
        }

        Physician savedPhysician = physicianRepository.save(physician);
        return convertToDTO(savedPhysician);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PhysicianOutputDTO> getPhysicianById(Long id) {
        return physicianRepository.findById(id).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PhysicianBasicInfoDTO> getPhysicianBasicInfoById(Long id) {
        return physicianRepository.findById(id).map(this::convertToBasicDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhysicianOutputDTO> searchPhysicians(String name, String specialtyName) {
        Specification<Physician> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), "%" + name.toLowerCase() + "%"));
            }
            if (specialtyName != null && !specialtyName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.join("specialty").get("name")), specialtyName.toLowerCase()));
            }
            if (predicates.isEmpty()) return criteriaBuilder.conjunction();
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return physicianRepository.findAll(spec).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhysicianBasicInfoDTO> searchPhysiciansForPatient(String name, String specialtyName) {
        Specification<Physician> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), "%" + name.toLowerCase() + "%"));
            }
            if (specialtyName != null && !specialtyName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.join("specialty").get("name")), specialtyName.toLowerCase()));
            }
            if (predicates.isEmpty()) return criteriaBuilder.conjunction();
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return physicianRepository.findAll(spec).stream()
                .map(this::convertToBasicDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PhysicianOutputDTO updatePhysician(Long id, RegisterPhysicianDTO physicianDTO) {
        Physician physicianToUpdate = physicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Physician not found with ID: " + id));

        if (!physicianToUpdate.getEmail().equalsIgnoreCase(physicianDTO.getEmail()) &&
                physicianRepository.findByEmail(physicianDTO.getEmail()).filter(p -> !p.getId().equals(id)).isPresent()) {
            throw new DuplicateResourceException("Email já existe para outro médico: " + physicianDTO.getEmail());
        }
        if (!physicianToUpdate.getPhoneNumber().equals(physicianDTO.getPhoneNumber()) &&
                physicianRepository.findByPhoneNumber(physicianDTO.getPhoneNumber()).filter(p -> !p.getId().equals(id)).isPresent()) {
            throw new DuplicateResourceException("Número de telefone já existe para outro médico: " + physicianDTO.getPhoneNumber());
        }

        Specialization specialty = specializationRepository.findById(physicianDTO.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with ID: " + physicianDTO.getSpecialtyId()));
        Department department = departmentRepository.findById(physicianDTO.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + physicianDTO.getDepartmentId()));

        LocalTime startTime;
        LocalTime endTime;
        try {
            startTime = LocalTime.parse(physicianDTO.getWorkStartTime(), timeFormatter);
            endTime = LocalTime.parse(physicianDTO.getWorkEndTime(), timeFormatter);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid time format for working hours. Use HH:mm. Details: " + e.getMessage(), e);
        }

        physicianToUpdate.setFullName(physicianDTO.getFullName());
        physicianToUpdate.setSpecialty(specialty);
        physicianToUpdate.setDepartment(department);
        physicianToUpdate.setEmail(physicianDTO.getEmail());
        physicianToUpdate.setPhoneNumber(physicianDTO.getPhoneNumber());
        physicianToUpdate.setAddress(physicianDTO.getAddress());
        physicianToUpdate.setWorkStartTime(startTime);
        physicianToUpdate.setWorkEndTime(endTime);
        physicianToUpdate.setOptionalDescription(physicianDTO.getOptionalDescription());

        Physician updatedPhysician = physicianRepository.save(physicianToUpdate);
        return convertToDTO(updatedPhysician);
    }
}