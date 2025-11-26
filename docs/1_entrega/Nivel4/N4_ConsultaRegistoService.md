# Business Layer Components

## Consulta Registo Service

**File:** [ConsultaRegistoServiceImpl](../../Clinical-Records-Service/src/main/java/com/LETI_SIDIS_3DA_2/clinical_records_service/service/ConsultaRegistoServiceImpl.java)  
**Technology:** Spring Boot Service + RestTemplate  
**Responsibility:** Manages creation and retrieval of clinical consultation records  
**Entity Managed:** `ConsultaRegisto`

---

### Overview
The `ConsultaRegistoServiceImpl` component is responsible for managing **clinical consultation records** within the Clinical Records Service.  
It provides business logic for creating, validating, and retrieving medical consultation records (`ConsultaRegisto`) associated with completed appointments.

This service interacts with the **Scheduling Service** to ensure that records can only be created for **completed consultations**, enforcing cross-service data consistency.

---

### Dependencies
- **Repository:** `ConsultaRegistoRepository` – Handles persistence of consultation records
- **External Service:** `Scheduling Service` (via `RestTemplate`) – Validates appointment status before record creation
- **DTOs:**
    - `CreateConsultaRegistoDTO` – Input data for creating records
    - `ConsultaRegistoOutputDTO` – Output data returned to the client
    - `AppointmentDetailsDTO` – Response model for data received from the Scheduling Service
- **Exceptions:**
    - `DuplicateResourceException` – Prevents creation of duplicate records
    - `ResourceNotFoundException` – Raised if the referenced appointment does not exist
    - `ServiceUnavailableException` – Handles connectivity or availability issues between services

---

### Layer Position
- **Layer:** Business / Service Layer
- **Above:** Controller Layer (REST endpoints for clinical records)
- **Below:** Data Access Layer (`ConsultaRegistoRepository`) and external Scheduling microservice

---

### Business Rules Implemented
- **Only completed consultations** can have records created (`status = COMPLETED`)
- **One-to-one constraint:** each consultation can have only one record
- **Cross-service validation:** verifies appointment existence and state via REST API call
- **Transaction management:** ensures atomic creation of records using `@Transactional`
- **Robust error handling** for communication failures and data consistency violations

---

### Key Methods

```java
@Service
public class ConsultaRegistoServiceImpl implements ConsultaRegistoService {

    private final ConsultaRegistoRepository recordRepository;
    private final RestTemplate restTemplate;

    @Value("${services.scheduling.url}")
    private String schedulingServiceUrl;

    @Transactional
    public ConsultaRegistoOutputDTO createRecord(CreateConsultaRegistoDTO dto) {
        AppointmentDetailsDTO appointment = getAppointmentDetails(dto.getConsultaId());

        if (!"COMPLETED".equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalArgumentException("Só é possível criar registos para consultas 'COMPLETED'.");
        }

        if (recordRepository.findByConsultaId(dto.getConsultaId()).isPresent()) {
            throw new DuplicateResourceException("Já existe um registo para a consulta com ID: " + dto.getConsultaId());
        }

        ConsultaRegisto record = new ConsultaRegisto(
            dto.getConsultaId(),
            dto.getDiagnosis(),
            dto.getTreatmentRecommendations(),
            dto.getPrescriptions(),
            LocalDateTime.now()
        );

        ConsultaRegisto savedRecord = recordRepository.save(record);
        return convertToDTO(savedRecord);
    }

    @Transactional(readOnly = true)
    public Optional<ConsultaRegistoOutputDTO> getRecordByConsultaId(Long consultaId) {
        return recordRepository.findByConsultaId(consultaId)
                .map(this::convertToDTO);
    }

    private ConsultaRegistoOutputDTO convertToDTO(ConsultaRegisto record) {
        ConsultaRegistoOutputDTO dto = new ConsultaRegistoOutputDTO();
        dto.setId(record.getId());
        dto.setConsultaId(record.getConsultaId());
        dto.setDiagnosis(record.getDiagnosis());
        dto.setTreatmentRecommendations(record.getTreatmentRecommendations());
        dto.setPrescriptions(record.getPrescriptions());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }

    private AppointmentDetailsDTO getAppointmentDetails(Long consultaId) {
        try {
            String url = schedulingServiceUrl + "/" + consultaId;
            AppointmentDetailsDTO appointmentDetails = restTemplate.getForObject(url, AppointmentDetailsDTO.class);
            if (appointmentDetails == null) {
                throw new ResourceNotFoundException("Consulta " + consultaId + " não encontrada no serviço de agendamento.");
            }
            return appointmentDetails;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Consulta " + consultaId + " não encontrada no serviço de agendamento.");
        } catch (Exception e) {
            throw new ServiceUnavailableException("Serviço de agendamento indisponível no momento.");
        }
    }
}
```
