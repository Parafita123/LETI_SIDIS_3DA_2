package com.LETI_SIDIS_3DA_2.physician_service.repository;

import com.LETI_SIDIS_3DA_2.physician_service.domain.Physician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhysicianRepository extends JpaRepository<Physician, Long>,
        JpaSpecificationExecutor<Physician> {
    Optional<Physician> findByEmail(String email);
    Optional<Physician> findByPhoneNumber(String phoneNumber);

    //adicionar método de pesquisa top 5 psiquiatras por nmr de appointments
}
