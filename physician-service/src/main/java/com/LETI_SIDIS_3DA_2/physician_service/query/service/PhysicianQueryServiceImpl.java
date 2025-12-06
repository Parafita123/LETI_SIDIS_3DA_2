package com.LETI_SIDIS_3DA_2.physician_service.query.service;

import com.LETI_SIDIS_3DA_2.physician_service.domain.Physician;
import com.LETI_SIDIS_3DA_2.physician_service.query.dto.PhysicianBasicInfoDTO;
import com.LETI_SIDIS_3DA_2.physician_service.query.dto.PhysicianOutputDTO;
import com.LETI_SIDIS_3DA_2.physician_service.repository.PhysicianRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PhysicianQueryServiceImpl implements PhysicianQueryService {

    private final PhysicianRepository physicianRepository;

    public PhysicianQueryServiceImpl(PhysicianRepository physicianRepository) {
        this.physicianRepository = physicianRepository;
    }

    // Helpers de mapeamento

    private PhysicianOutputDTO convertToDTO(Physician physician) {
        if (physician == null) return null;

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
        if (physician == null) return null;
        return new PhysicianBasicInfoDTO(
                physician.getFullName(),
                physician.getSpecialty() != null ? physician.getSpecialty().getName() : null
        );
    }

    // --- queries ---

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
        Specification<Physician> spec = buildSearchSpecification(name, specialtyName);
        return physicianRepository.findAll(spec).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhysicianBasicInfoDTO> searchPhysiciansForPatient(String name, String specialtyName) {
        Specification<Physician> spec = buildSearchSpecification(name, specialtyName);
        return physicianRepository.findAll(spec).stream()
                .map(this::convertToBasicDTO)
                .collect(Collectors.toList());
    }

    private Specification<Physician> buildSearchSpecification(String name, String specialtyName) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.trim().isEmpty()) {
                predicates.add(
                        cb.like(cb.lower(root.get("fullName")),
                                "%" + name.toLowerCase() + "%")
                );
            }
            if (specialtyName != null && !specialtyName.trim().isEmpty()) {
                predicates.add(
                        cb.equal(cb.lower(root.join("specialty").get("name")),
                                specialtyName.toLowerCase())
                );
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
