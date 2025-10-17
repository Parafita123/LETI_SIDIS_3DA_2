package com.psoft2024._5.grupo1.projeto_psoft.repository;
import com.psoft2024._5.grupo1.projeto_psoft.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConsultasRegistoRepository extends JpaRepository<ConsultaRegisto, Long>{


    Optional<ConsultaRegisto> findByConsulta(Consulta consulta);
    List<ConsultaRegisto> findByConsultaId(Long consultaId);
    Optional<ConsultaRegisto> findFirstByConsultaOrderByCreatedAtDesc(Consulta consulta);
}

