package com.LETI_SIDIS_3DA_2.clinical_records_service.repository;

import com.LETI_SIDIS_3DA_2.clinical_records_service.domain.ConsultaRegisto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsultaRegistoRepository extends JpaRepository<ConsultaRegisto, Long> {
    // Altera o método findByConsulta para findByConsultaId
    Optional<ConsultaRegisto> findByConsultaId(Long consultaId);

    // Um método para encontrar todos os registos de um paciente seria mais complexo
    // e depois procurar os registos por esses IDs de consulta.
    // Ex: List<ConsultaRegisto> findByConsultaIdIn(List<Long> consultaIds);
}