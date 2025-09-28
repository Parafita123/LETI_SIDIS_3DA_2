package com.psoft.clinic.speciality.services;

import com.psoft.clinic.speciality.model.Speciality;
import com.psoft.clinic.speciality.repository.SpringBootSpecialityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpecialityService {

    private final SpringBootSpecialityRepository specialityRepository;

    public Speciality create(CreateSpecialityRequest request) {
        Speciality speciality = new Speciality();
        speciality.setName(request.getName());
        speciality.setDescription(request.getDescription());
        speciality.setAcronym(request.getAcronym());
        return specialityRepository.save(speciality);
    }

    public Optional<Speciality> findByName(String name) {
        return specialityRepository.findByName(name);
    }
}
