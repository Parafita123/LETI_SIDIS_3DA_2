package com.LETI_SIDIS_3DA2.scheduling_service.repository;

import com.LETI_SIDIS_3DA2.scheduling_service.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    // Método adaptado para usar o ID do médico
    List<Consulta> findByPhysicianIdAndDateTimeBetween(Long physicianId, LocalDateTime start, LocalDateTime end);
    List<Consulta> findByPatientId(Long patientId);
    List<Consulta> findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime dateTime);
    List<Consulta> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Consulta> findByPhysicianIdAndDateTime(Long physicianId, LocalDateTime dateTime);
}



