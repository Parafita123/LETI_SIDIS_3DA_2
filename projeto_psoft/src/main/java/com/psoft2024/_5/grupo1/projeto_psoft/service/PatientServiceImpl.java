package com.psoft2024._5.grupo1.projeto_psoft.service;

import com.psoft2024._5.grupo1.projeto_psoft.domain.Patient;
import com.psoft2024._5.grupo1.projeto_psoft.dto.PatientCreateDto;
import com.psoft2024._5.grupo1.projeto_psoft.dto.PatientUpdateDto;
import com.psoft2024._5.grupo1.projeto_psoft.dto.PatientDto;
import com.psoft2024._5.grupo1.projeto_psoft.exception.ResourceNotFoundException;
import com.psoft2024._5.grupo1.projeto_psoft.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository repo;

    public PatientServiceImpl(PatientRepository repo) {
        this.repo = repo;
    }

    @Override
    public PatientDto register(PatientCreateDto in) {
        Patient p = new Patient();

        // Preenche os campos originais obrigatórios (@NotBlank)
        p.setName(in.fullName);
        p.setBirthDate(in.birthDateLocal.toString());

        // Preenche os novos campos do WP2A
        p.setFullName(in.fullName);
        p.setEmail(in.email);
        p.setBirthDateLocal(in.birthDateLocal);
        p.setPhoneNumber(in.phoneNumber);
        p.setInsurancePolicyNumber(in.insurancePolicyNumber);
        p.setInsuranceCompany(in.insuranceCompany);
        p.setConsentDate(in.consentDate);
        p.setPhotoUrl(in.photoUrl);
        p.setHealthConcerns(in.healthConcerns);

        Patient saved = repo.save(p);
        return toDto(saved);


    }

    @Override
    public PatientDto update(Long id, PatientUpdateDto in) {
        Patient p = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

        // Só os campos que o paciente pode alterar:
        p.setEmail(in.email);
        p.setPhoneNumber(in.phoneNumber);
        p.setInsuranceCompany(in.insuranceCompany);
        p.setInsurancePolicyNumber(in.insurancePolicyNumber);
        p.setPhotoUrl(in.photoUrl);               // se adicionaste esse campo
        p.setHealthConcerns(in.healthConcerns);   // se adicionaste esse campo
        // (Não toca em fullName, birthDateLocal, consentDate… a menos que queiras permitir)

        Patient saved = repo.save(p);
        return toDto(saved);
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
