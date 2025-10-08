package com.psoft2024._5.grupo1.projeto_psoft.repository;
import com.psoft2024._5.grupo1.projeto_psoft.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.psoft2024._5.grupo1.projeto_psoft.dto.PhysicianAppointmentCountDTO;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConsultasRepository extends JpaRepository<Consulta, Long>{

    List<Consulta> findByPatient(Patient patient);
    Optional<Consulta> findByPatientAndPhysicianAndDateTime(Patient patient,Physician physician, LocalDateTime dateTime);
    List<Consulta> findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime now);
    Optional<Object> findByPhysicianAndDateTime(Physician physician, LocalDateTime dateTime);
    @Query("SELECT new com.psoft2024._5.grupo1.projeto_psoft.dto.PhysicianAppointmentCountDTO(c.physician.id, c.physician.fullName, COUNT(c.id)) " +
            "FROM Consulta c " +
            "GROUP BY c.physician.id, c.physician.fullName " +
            "ORDER BY COUNT(c.id) DESC")
    List<PhysicianAppointmentCountDTO> findTopPhysiciansByAppointmentCount(Pageable pageable);
    List<Consulta> findByPhysicianAndDateTimeBetween(Physician physician, LocalDateTime startDateTime, LocalDateTime endDateTime);
    boolean existsByPhysicianAndDateTime(Physician physician, LocalDateTime dateTime);

}


