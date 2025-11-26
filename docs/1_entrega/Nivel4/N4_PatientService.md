# Business Layer Components

## Patient Service

**File:** [PatientService](../../../Patient-Service/src/main/java/com/LETI_SIDIS_3DA2/Patient_Service/service/PatientServiceImpl.java)  
**Technology:** Spring Service  
**Responsibility:** Implements patient business logic  
**Base Endpoint:** `/api/patients`

---

### Overview
The `PatientService` component manages all patient-related business operations such as registration, update, and retrieval.  
It bridges the Controller layer and the Repository layer, enforcing business rules and handling exceptions.


---

### Dependencies
- **Repository:** `PatientRepository` for data persistence
- **DTOs:** `PatientDto`, `PatientCreateDto`, `PatientUpdateDto` for data transfer
- **Exception:** `ResourceNotFoundException` for missing patient entities

---

### Layer Position
- **Layer:** Business / Service Layer
- **Above:** Controller Layer (`PatientController`)
- **Below:** Data Access Layer (`PatientRepository`)

---

### Business Rules Implemented
- Manual DTO-to-Entity mapping
- Exception handling for not found resources
- Read/Write operations orchestration
- No interface segregation (direct implementation)
- Enforces domain separation (no coupling with User or Physician entities)
- Uses DTOs to prevent entity exposure
- Implements Single Responsibility and consistent exception handling

---

### Key Methods

```java
@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository repo;

    public PatientServiceImpl(PatientRepository repo) {
        this.repo = repo;
    }

    @Override
    public PatientDto register(PatientCreateDto in) {
        Patient p = new Patient();
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
        return toDto(saved);
    }

    @Override
    public PatientDto update(Long id, PatientUpdateDto in) {
        Patient p = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

        p.setEmail(in.email);
        p.setPhoneNumber(in.phoneNumber);
        p.setInsuranceCompany(in.insuranceCompany);
        p.setInsurancePolicyNumber(in.insurancePolicyNumber);
        p.setPhotoUrl(in.photoUrl);
        p.setHealthConcerns(in.healthConcerns);

        Patient saved = repo.save(p);
        return toDto(saved);
    }

    @Override
    public PatientDto findById(Long id) {
        Patient p = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
        return toDto(p);
    }

    @Override
    public List<PatientDto> searchByName(String name) {
        return repo.findByFullNameContainingIgnoreCase(name)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
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
```