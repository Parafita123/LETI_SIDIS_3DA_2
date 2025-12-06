package com.LETI_SIDIS_3DA2.Patient_Service.query.service;

import com.LETI_SIDIS_3DA2.Patient_Service.domain.Patient;
import com.LETI_SIDIS_3DA2.Patient_Service.exception.ResourceNotFoundException;
import com.LETI_SIDIS_3DA2.Patient_Service.query.dto.PatientDto;
import com.LETI_SIDIS_3DA2.Patient_Service.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PatientQueryServiceImpl implements PatientQueryService {

    private final PatientRepository repo;

    public PatientQueryServiceImpl(PatientRepository repo) {
        this.repo = repo;
    }

    @Override
    public PatientDto findById(Long id) {
        Patient p = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
        return toDto(p);
    }

    @Override
    public List<PatientDto> searchByName(String name) {
        return repo.findByFullNameContainingIgnoreCase(name).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private PatientDto toDto(Patient p) {
        PatientDto d = new PatientDto();
        d.id = p.getId();
        d.fullName = p.getFullName();
        d.email = p.getEmail();
        d.birthDateLocal = p.getBirthDateLocal();
        d.phoneNumber = p.getPhoneNumber();
        d.insurancePolicyNumber = p.getInsurancePolicyNumber();
        d.insuranceCompany = p.getInsuranceCompany();
        d.consentDate = p.getConsentDate();
        d.photoUrl = p.getPhotoUrl();
        d.healthConcerns = p.getHealthConcerns();
        return d;
    }
}
