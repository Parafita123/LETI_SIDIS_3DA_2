package com.LETI_SIDIS_3DA2.Patient_Service.command.service;

import com.LETI_SIDIS_3DA2.Patient_Service.command.dto.PatientCreateDto;
import com.LETI_SIDIS_3DA2.Patient_Service.command.dto.PatientUpdateDto;
import com.LETI_SIDIS_3DA2.Patient_Service.domain.Patient;
import com.LETI_SIDIS_3DA2.Patient_Service.exception.ResourceNotFoundException;
import com.LETI_SIDIS_3DA2.Patient_Service.messaging.PatientEventPayload;
import com.LETI_SIDIS_3DA2.Patient_Service.messaging.PatientEventPublisher;
import com.LETI_SIDIS_3DA2.Patient_Service.query.dto.PatientDto;
import com.LETI_SIDIS_3DA2.Patient_Service.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PatientCommandServiceImpl implements PatientCommandService {

    private final PatientRepository repo;
    private final PatientEventPublisher eventPublisher;

    public PatientCommandServiceImpl(PatientRepository repo, PatientEventPublisher eventPublisher ) {
        this.repo = repo;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PatientDto register(PatientCreateDto in) {
        Patient p = new Patient();

        // campos originais
        p.setName(in.fullName);
        p.setBirthDate(in.birthDateLocal.toString());

        // campos do WP2A
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

        PatientEventPayload payload = new PatientEventPayload(
                saved.getId(),
                saved.getFullName(),
                saved.getEmail(),
                saved.getBirthDateLocal(),
                saved.getPhoneNumber(),
                saved.getInsuranceCompany(),
                saved.getInsurancePolicyNumber(),
                saved.getPhotoUrl(),
                saved.getHealthConcerns()
        );

        eventPublisher.publish("patient.registered", "PatientRegistered", payload);

        return toDto(saved);
    }

    @Override
    public PatientDto update(Long id, PatientUpdateDto in) {
        Patient p = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

        // só os campos alteráveis
        p.setEmail(in.email);
        p.setPhoneNumber(in.phoneNumber);
        p.setInsuranceCompany(in.insuranceCompany);
        p.setInsurancePolicyNumber(in.insurancePolicyNumber);
        p.setPhotoUrl(in.photoUrl);
        p.setHealthConcerns(in.healthConcerns);

        Patient saved = repo.save(p);

        PatientEventPayload payload = new PatientEventPayload(
                saved.getId(),
                saved.getFullName(),
                saved.getEmail(),
                saved.getBirthDateLocal(),
                saved.getPhoneNumber(),
                saved.getInsuranceCompany(),
                saved.getInsurancePolicyNumber(),
                saved.getPhotoUrl(),
                saved.getHealthConcerns()
        );

        eventPublisher.publish("patient.updated", "PatientUpdated", payload);

        return toDto(saved);
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