package com.psoft.clinic.appointmentsmanagement.repository;

import com.psoft.clinic.appointmentsmanagement.model.Appointment;
import com.psoft.clinic.appointmentsmanagement.model.AppointmentStatus;
import com.psoft.clinic.appointmentsmanagement.services.PhysicianCount;
import com.psoft.clinic.patientmanagement.model.Patient;
import com.psoft.clinic.physiciansmanagement.model.Physician;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SpringBootAppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByPhysicianAndDateAndStartTime(Physician physician, LocalDate date, LocalTime start);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Appointment a " +
            "WHERE a.date = :date " +
            "  AND a.startTime = :startTime " +
            "  AND a.PatientFullName = :patientFullName " +
            "  AND a.PhysicianFullName = :physicianFullName")
    boolean checkExists(
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("patientFullName") String patientFullName,
            @Param("physicianFullName") String physicianFullName
    );

    @Query("""
      SELECT a
      FROM Appointment a
      WHERE a.PhysicianFullName = :physicianFullName
        AND a.date              = :date
    """)
    List<Appointment> findByPhysicianAndDate(
            @Param("physicianFullName") String physicianFullName,
            @Param("date") LocalDate date
    );

    @Query("""
        SELECT new com.psoft.clinic.appointmentsmanagement.services.PhysicianCount(
            p.id,
            a.PhysicianFullName,
            COUNT(a)
        )
        FROM Appointment a
        JOIN a.physician p
        WHERE a.date BETWEEN :startDate AND :endDate
        GROUP BY p.id, a.PhysicianFullName
        ORDER BY COUNT(a) DESC
    """)
    List<PhysicianCount> findTop5PhysiciansByDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate")   LocalDate endDate,
            Pageable pageable
    );

    @Query("""
        SELECT a 
          FROM Appointment a 
         WHERE a.patient = :patient 
           AND a.physician.baseUser.fullName = :physicianName
    """)
    List<Appointment> findByPatientAndPhysicianName(
            @Param("patient") Patient patient,
            @Param("physicianName") String physicianName
    );


    @EntityGraph(attributePaths = {"patient"})
    @Query("SELECT a FROM Appointment a")
    List<Appointment> findAllWithPatient();


    List<Appointment> findByPatient(Patient patient);

    List<Appointment> findByDateGreaterThanEqualAndStatusInOrderByDateAscStartTimeAsc(
            LocalDate date, List<AppointmentStatus> statuses);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE YEAR(a.date) = :year AND MONTH(a.date) = :month")
    long countByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE YEAR(a.date) = :year AND MONTH(a.date) = :month AND a.status = :status")
    long countByYearAndMonthAndStatus(@Param("year") int year, @Param("month") int month, @Param("status") AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE YEAR(a.date) = :year AND MONTH(a.date) = :month ORDER BY a.date ASC")
    List<Appointment> findByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT a.physician.baseUser.fullName, a.startTime, a.endTime " +
            "FROM Appointment a " +
            "WHERE a.startTime IS NOT NULL AND a.endTime IS NOT NULL")
    List<Object[]> findAppointmentTimesPerPhysician();

    List<Appointment> findByPhysicianIdAndDate(Long physicianId, LocalDate date);

    List<Appointment> findByStatusIn(List<AppointmentStatus> statuses);

}

