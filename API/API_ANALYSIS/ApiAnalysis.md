# Clinic API Documentation

## Base URL

`http://<host>:<port>`

---

## Authentication

### Login

- **Endpoint:** `POST /api/public/login`
- **Description:** Autentica um usuário (Admin, Patient ou Physician) e retorna um JWT no cabeçalho `Authorization`.
- **Request Body:**

  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```

- **Response:**
    - **Status 200 OK**
        - **Headers:**
            - `Authorization: Bearer <token>`
        - **Body:** JSON com os dados do usuário autorizado.
    - **Status 401 Unauthorized** — credenciais inválidas.

### Register (Patient)

- **Endpoint:** `POST /api/public/register`
- **Description:** Registra um novo paciente.
- **Request Body:**

  ```json
  {
    "username": "string",
    "password": "string",
    "fullName": "string",
    "gender": "MALE" | "FEMALE" | "OTHER",
    "email": "string",
    "street": "string",
    "city": "string",
    "district": "string",
    "zip": "string",
    "country": "string",
    "phoneNumber": "string",
    "phoneType": "MOBILE" | "HOME" | "WORK",
    "dateOfBirth": "dd/MM/yyyy",
    "insuranceProvider": "string (opcional)",
    "policyNumber": "string (opcional)",
    "consent": "string (opcional)"
  }
  ```

- **Response:**
    - **Status 201 Created** — retorna view do paciente criado.
    - **Status 400 Bad Request** — erros de validação.

---

## Security Configuration

A configuração de segurança define quais papéis podem acessar cada conjunto de endpoints:

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()
        .authorizeRequests()
        .antMatchers("/api/public/**").permitAll()
        .antMatchers("/api/admin/**").hasRole("USER_ADMIN")
        .antMatchers("/api/patient/**").hasRole("USER_PATIENT")
        .antMatchers("/api/physician/**").hasRole("USER_PHYSICIAN")
        .anyRequest().authenticated();
}
```

- **/api/public/** — acesso sem autenticação.
- **/api/admin/** — apenas usuários com papel **USER_ADMIN**.
- **/api/patient/** — apenas usuários com papel **USER_PATIENT**.
- **/api/physician/** — apenas usuários com papel **USER_PHYSICIAN**.

---

## Public Endpoints

### POST /api/public/login

- **Descrição:** Autentica usuário e retorna JWT.

### POST /api/public/register

- **Descrição:** Registra um novo paciente.

---

## Admin Endpoints (Role: USER_ADMIN)

> **Requer header:**
>
> `Authorization: Bearer <token>`

### POST /api/admin/create/physician

- **Descrição:** Cria um novo physician.
- **Request Body:** JSON com dados do physician.
- **Responses:**
    - 200 OK — physician criado.
    - 400 Bad Request — dados inválidos.
    - 500 Internal Server Error — erro interno.

### GET /api/admin/search/physician/{id}

- **Descrição:** Busca physician por ID.
- **Path:** `id` (Long)
- **Responses:**
    - 200 OK — dados do physician.
    - 404 Not Found — não encontrado.

### GET /api/admin/search/patient/{id}

- **Descrição:** Busca patient por ID.
- **Path:** `id` (Long)
- **Responses:**
    - 200 OK — dados do paciente.
    - 404 Not Found — não encontrado.

### GET /api/admin/search/patient?term={term}

- **Descrição:** Busca pacientes pelo nome.
- **Query:** `term` (String)
- **Response:** 200 OK — lista de pacientes.

---

## Patient Endpoints (Role: USER_PATIENT)

> **Requer header:**
>
> `Authorization: Bearer <token>`

### GET /api/patient/search/physicians?term={term}

- **Descrição:** Busca physicians por nome ou especialidade.
- **Query:** `term` (String)
- **Response:** 200 OK — lista de physicians.

---

> **Observação:** Inclua sempre `Content-Type: application/json` nas requisições com corpo.
