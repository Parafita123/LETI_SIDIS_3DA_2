# Data Access Layer Components

## Scheduling Repository

**File:** [ConsultaRepository](../../Scheduling-Service/src/main/java/com/LETI_SIDIS_3DA2/scheduling_service/repository/ConsultaRepository.java)  
**Technology:** Spring Data JPA  
**Responsibility:** Manages persistence and retrieval of scheduling and consultation data  
**Entity Managed:** `Consulta`

---

### Overview
The `ConsultaRepository` is part of the **data access layer** of the *Scheduling Service*.  
It provides a JPA-based abstraction for all database operations related to medical appointments (consultas).

This repository encapsulates all database queries for **physicians**, **patients**, and **time-based lookups**, enabling the service layer to handle scheduling logic without worrying about SQL details.

---

### Dependencies
- **Entity:** `Consulta` – Represents a scheduled appointment between a physician and a patient
- **Framework:** `Spring Data JPA` – Generates queries automatically from method names
- **Database:** Independent scheduling database containing the `consultas` table
- **Domain Services:** Used by `SchedulingServiceImpl` for business logic operations

---

### Layer Position
- **Layer:** Data Access / Repository Layer
- **Above:** Business Layer (`SchedulingServiceImpl`)
- **Below:** Database (typically PostgreSQL or H2 for tests)

---

### Key Responsibilities
- Encapsulates access to scheduling data (no direct SQL in service layer)
- Supports **query derivation** by method name (Spring Data JPA)
- Provides efficient filtering by:
    - Physician ID
    - Patient ID
    - Time ranges (for availability checks and planning)
- Enables **temporal queries** crucial for avoiding overlapping appointments
- Maintains separation of concerns: pure data access, no business logic

---

### Key Methods

```java
@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    // Find all consultations for a specific physician within a given time period
    List<Consulta> findByPhysicianIdAndDateTimeBetween(Long physicianId, LocalDateTime start, LocalDateTime end);

    // Retrieve all consultations of a specific patient
    List<Consulta> findByPatientId(Long patientId);

    // Get all upcoming consultations ordered chronologically
    List<Consulta> findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime dateTime);

    // List all consultations occurring within a defined time window
    List<Consulta> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);

    // Find a consultation by physician and exact date/time (used to detect conflicts)
    List<Consulta> findByPhysicianIdAndDateTime(Long physicianId, LocalDateTime dateTime);
}
