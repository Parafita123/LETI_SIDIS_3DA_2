# Business Layer Components

## Physician Controller

**File:** [PhysicianController](../../Physician-Service/src/main/java/com/LETI_SIDIS_3DA_2/physician_service/controller/PhysicianController.java)  
**Technology:** Spring REST Controller  
**Responsibility:** Handles HTTP requests and responses for physician management (CRUD operations, profile photo handling, and search)  
**Base Endpoint:** `/api/physicians`

---

### Overview
The `PhysicianController` represents the **presentation layer** of the *Physician Service*.  
It exposes REST endpoints for managing physician entities, delegating business logic to the `PhysicianService`, and file-related tasks to the `FileStorageService`.  
Unlike standard controllers, it also handles **multipart/form-data** requests, enabling profile photo uploads and downloads through HTTP.

This controller ensures **data validation**, **role-based access**, and **structured JSON error responses**, maintaining separation of concerns and security compliance across user roles (`ADMIN`, `PATIENT`, `AUTHENTICATED`).

---

### Dependencies
- **Service:** `PhysicianService` – Executes business rules and data operations
- **Service:** `FileStorageService` – Manages photo storage and file retrieval
- **DTOs:** `RegisterPhysicianDTO`, `PhysicianOutputDTO`, `PhysicianBasicInfoDTO` – Data exchange between layers
- **Utility:** `ObjectMapper` – Converts embedded JSON strings in multipart requests
- **Security:** `@PreAuthorize` annotations – Role-based access enforcement
- **Exception Handling:** `ResponseStatusException` – Translates service errors into HTTP status codes

---

### Layer Position
- **Layer:** Presentation / Controller Layer
- **Above:** External clients (Admin Dashboard, Patient Portal, API Consumers)
- **Below:** Business / Service Layer (`PhysicianService`, `FileStorageService`)

---

### Business Rules Implemented
- Validates physician registration data and forwards it to the service layer
- Supports **file uploads and streaming responses** for profile photo management
- Implements **role-based access control (RBAC)**:
    - `ADMIN`: Full CRUD and update operations
    - `PATIENT`: Limited physician search and detail access
    - `AUTHENTICATED`: Restricted data visibility
- Delegates business and storage responsibilities to respective services, promoting **Single Responsibility Principle (SRP)**
- Provides **uniform exception translation** (`400`, `404`, `409`, `500`) for consistent API behavior

---

### Key Methods

```java
@RestController
@RequestMapping("/api/physicians")
public class PhysicianController {

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PhysicianOutputDTO> registerPhysician(
            @RequestPart("physicianData") String physicianData,
            @RequestPart(name = "profilePhoto", required = false) MultipartFile file) { ... }

    @GetMapping("/photo/{filename:.+}")
    public ResponseEntity<Resource> getPhysicianProfilePhoto(@PathVariable String filename) { ... }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PATIENT') or isAuthenticated()")
    public ResponseEntity<?> getPhysicianDetailsById(@PathVariable Long id, Authentication auth) { ... }

    @GetMapping("/search")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<PhysicianBasicInfoDTO>> searchPhysiciansForPatient(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String specialty) { ... }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PhysicianOutputDTO> updatePhysician(
            @PathVariable Long id, @RequestBody RegisterPhysicianDTO dto) { ... }
}
```
---


