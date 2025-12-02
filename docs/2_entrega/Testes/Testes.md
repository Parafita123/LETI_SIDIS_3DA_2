
# 0) Objetivo do Documento

Este documento define o plano de testes funcional a aplicar aos serviços da arquitetura distribuída.
## Especifica:

* O que testar em cada serviço;

* Testes mínimos de aceitação;

* Pré-condições;

* Entradas válidas e inválidas;

* Códigos de resposta esperados;

* Exemplos de requests.

Funciona como contrato de QA e como validação final da migração monolítica → microserviços.

# 1) Estratégia de Testes

| Tipo de Teste                          | Foco              | Serviços                            
|----------------------------------------|-------------------|-------------------------------------
| **Funcional**                          | **Validação de endpoints, DTOs e lógicas**      | Todos                               
| **Autorização**                        | **JWT + roles + permissões**     | Todos     
| **Integração**                         | **Cross-Service	Physician ↔ Scheduling ↔ Records**     | Scheduling, Records                               
| **Validação de Erros**                 | **400/401/403/404/409**     | Todos                               
| **Via Gateway**                        | **Roteamento + propagação de headers**       | Gateway              


Cada serviço tem dois testes fundamentais:
* Um teste de sucesso (caminho feliz)
* Um teste negativo (comportamento esperado em erro)

# 2) Matriz Serviço ↔ Tipos de Teste


| Serviço                                | Testes Positivos                | Testes Negattivos                        | Notas                              |
|----------------------------------------|---------------------------------|------------------------------------------|------------------------------------|
| **Identity**                           | **Login -> 200 + token válido** | **Credenciais erradas -> 401**           | Token contém authorities           |
| **Physician**                          | **Criar e obter médico**        | **Criar sem token/role → 403**           | Dados devem propagar p/ Scheduling |
| **Patient**                            | **Criar paciente**              | **Pesquisar sem nome → 400**             | Base para Scheduling/Records       |
| **Scheduling**                         | **Criar consulta**              | **Cancelar com token errado → 403**      | Integra Physician & Patient        |
| **Records**                            | **Criar registo clínico**       | **Criar com consulta inexistente → 404** | Valida integridade clínica         |
| **Gateway**                            | **Roteamento de /api/...**      | **Rota inexistente → 404**               | Propagação Authorization           |



# 3) Testes por Serviço
   ## 3.1 Identity Service

Port: 8085
BasePath: /api/auth

* Teste I1 — Login com credenciais válidas

* Objetivo: Confirmar emissão de token JWT válido com claim "authorities".

POST /api/auth/login
Content-Type: application/json

{
"username": "patient1",
"password": "password123"
}


Esperado

{
"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
"tokenType": "Bearer"
}

* Teste I2 — Login inválido
POST /api/auth/login
Content-Type: application/json

{
"username": "patient1",
"password": "errado"
}


Esperado:
401 Unauthorized.

## 3.2 Physician Service

Port: 8081
BasePath: /api/physicians

* Teste P1 — Criar médico (Admin)
POST /api/physicians
Authorization: Bearer {{TOKEN_ADMIN}}
Content-Type: application/json

{
"fullName": "Dr. João Ribeiro",
"specialty": "Pediatrics",
"email": "joao.ribeiro@hospital.pt",
"phoneNumber": "912345678"
}


Esperado:
201 Created.

* Teste P2 — Criar médico sem permissões

Usar {{TOKEN_PATIENT}}.

Esperado:
403 Forbidden.

## 3.3 Patient Service

Port: 8083
BasePath: /api/patients

* Teste PT1 — Criar paciente
POST /api/patients
Content-Type: application/json

{
"fullName": "Maria Santos",
"email": "maria.santos@example.com",
"birthDateLocal": "2001-05-15",
"phoneNumber": "910000000"
}


Esperado:
201 Created.

* Teste PT2 — Pesquisa sem parâmetro obrigatório
GET /api/patients


Esperado:
400 Bad Request.

## 3.4 Scheduling Service

Port: 8084
BasePath: /api/consultas

* Teste S1 — Criar consulta
POST /api/consultas
Authorization: Bearer {{TOKEN_PATIENT}}
Content-Type: application/json

{
"patientId": 7,
"physicianId": 4,
"dateTime": "2025-07-15 10:00:00",
"consultationType": "General Checkup"
}


Esperado:
201 Created.

* Teste S2 — Cancelar consulta que não lhe pertence
PUT /api/consultas/15/cancelar
Authorization: Bearer {{TOKEN_PATIENT_OUTRO}}


Esperado:
403 Forbidden.

## 3.5 Clinical Records Service

BasePath: /api/records

* Teste R1 — Criar registo clínico
POST /api/records
Authorization: Bearer {{TOKEN_PHYSICIAN}}
Content-Type: application/json

{
"consultaId": 15,
"diagnosis": "Sinusite",
"treatmentRecommendations": "Repouso, fluidos",
"prescriptions": "Ibuprofeno"
}


Esperado:
201 Created.

* Teste R2 — Criar registo para consulta inexistente
{
"consultaId": 9999,
"diagnosis": "Teste",
"treatmentRecommendations": "Teste",
"prescriptions": "Teste"
}


Esperado:
404 Not Found.

## 3.6 API Gateway (8086)
* Teste G1 — Roteamento + Propagação do Token

Executar o Teste S1 via Gateway:

POST {{GATEWAY}}/api/consultas
Authorization: Bearer {{TOKEN_PATIENT}}
...


Esperado:
201 Created.

* Teste G2 — Rota inexistente
GET {{GATEWAY}}/api/consulta


Esperado:
404 Not Found.

# 4) Anexos — Exemplos JSON 
## Consulta criada
   {
   "id": 15,
   "dateTime": "2025-07-15T10:00:00",
   "consultationType": "General Checkup",
   "status": "SCHEDULED",
   "notes": null,
   "patientId": 7,
   "patientName": "Maria Santos",
   "physicianId": 4,
   "physicianName": "Dr. João Ribeiro"
   }

## Registo clínico
{
"id": 5,
"consultaId": 15,
"diagnosis": "Sinusite",
"treatmentRecommendations": "Repouso e hidratação",
"prescriptions": "Ibuprofeno",
"createdAt": "2025-07-15T11:00:00"
}