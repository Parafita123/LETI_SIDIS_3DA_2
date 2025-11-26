# Security Layer Components

## Auth Controller

**File:** [AuthController](../../Identity-Service/src/main/java/com/LETI_SIDIS_3DA2/identity_service/controller/AuthController.java)  
**Technology:** Spring Boot REST Controller + Spring Security  
**Responsibility:** Handles user authentication and JWT generation  
**Endpoints:** `POST /api/auth/login`

---

### Overview
The `AuthController` component provides the **authentication endpoint** for the Anonymous (Identity) Service.  
It validates user credentials, delegates authentication to the Spring Security framework, and issues a **JWT (JSON Web Token)** for authorized access to other services.

This controller is the main entry point for users attempting to log in to the system, enabling secure and stateless communication across distributed microservices.

---

### Dependencies
- **`AuthenticationManager`** – Performs credential validation within the Spring Security context
- **`JwtUtil`** – Utility class for token generation and validation
- **`UserDetailsServiceImpl`** – Loads user-specific data from the `system_users` database
- **DTOs:**
    - `LoginRequest` – Contains username and password
    - `LoginResponse` – Returns the generated JWT token

---

### Layer Position
- **Layer:** Security / Presentation Layer
- **Above:** External clients (frontends or other services consuming the API)
- **Below:** Authentication framework (`AuthenticationManager`, `UserDetailsServiceImpl`, `JwtUtil`)

---

### Security Rules Implemented
- **Username and Password authentication** via `AuthenticationManager`
- **JWT Token generation** after successful login
- **Error handling** for invalid credentials
- Stateless authentication (no session stored on the server)
- Token reuse for authorization in subsequent requests

---

### Key Methods

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthController(AuthenticationManager authManager,
                          JwtUtil jwtUtil,
                          UserDetailsServiceImpl userDetailsService) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Usuário ou senha inválidos");
        }

        UserDetails ud = userDetailsService.loadUserByUsername(req.getUsername());
        String token = jwtUtil.generateToken(ud);
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
```