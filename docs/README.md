
> Objetivo: antes de mover qualquer ficheiro de código, fixar **o que pertence a cada serviço**, **como falam entre si** e **o que prometem externamente**. Este documento será o vosso “acordo de API” e guia de migração.

---

## 0) Contexto resumido

* Sistema original: monolítico (Spring Boot + JPA) com domínios Patients, Physicians, Consultas (agendamento) e Registos de Consulta (conteúdo clínico).
* Novo alvo: cinco serviços independentes, cada um com **base de dados própria** e comunicação **HTTP/JSON** através de **API Gateway**.
* Filipe Parafita trata de:

  * **Physician Service** (médicos, departamentos, especializações).
  * **Consultation Records Service** (registos clínicos de consultas).

---

## 1) Matriz de propriedade por domínio (RACI simplificado)

| Domínio / Artefacto                    | Serviço Dono      | Consumidores                        | Notas                                                                           |
| -------------------------------------- | ----------------- | ----------------------------------- | ------------------------------------------------------------------------------- |
| Utilizador/Identidade (JWT, roles)     | **Identity**      | Todos                               | Emite tokens. Chave pública partilhada.                                         |
| **Physician**                          | **Physician**     | Consultations, Records, Patient     | CRUD completo. Exposição de informação básica e detalhe.                        |
| **Department**                         | **Physician**     | Todos                               | Tabela de referência.                                                           |
| **Specialization**                     | **Physician**     | Todos                               | Tabela de referência.                                                           |
| **Patient**                            | **Patient**       | Consultations, Records              | CRUD completo.                                                                  |
| **Consulta (agendamento)**             | **Consultations** | Records, Patient, Physician         | Slots/estado (CREATED, CONFIRMED, CANCELLED, DONE).                             |
| **ConsultaRegisto (conteúdo clínico)** | **Records**       | Admin, Patient (leitura autorizada) | Criação/consulta/edição limitada; contém diagnóstico, observações, prescrições. |

**Regras:**

* Nenhum serviço lê a base de dados de outro serviço.
* Referências cruzadas são **IDs imutáveis** (UUID/long) guardados como campos simples (ex.: `physicianId`, `patientId`, `consultaId`).
* Qualquer validação cruzada faz‑se por **HTTP**.

---

## 2) Modelos canónicos (IDs e referências)

* `PhysicianId`, `PatientId`, `ConsultaId`, `RecordId` → tipo `String` (UUID).
* Convenção JSON: `camelCase`.
* Timestamps em **UTC ISO‑8601** (ex.: `2025-10-15T13:45:00Z`).

---

## 3) Contratos de API 

### 3.1 Physician Service — `/api/v1`

**DTOs usados**

* `PhysicianOutputDTO` `{ id, name, email, departmentId, specializations: [specId], phone?, room? }`
* `PhysicianBasicInfoDTO` `{ id, name, departmentId }`
* `DepartmentDTO` `{ id, name }`
* `SpecializationDTO` `{ id, name }`

**Endpoints**

* `GET api/physicians/{id}` → `PhysicianOutputDTO`
* `GET api/physicians` (query: `name?`, `departmentId?`, `specializationId?`, `page?`, `size?`) → `{ content: [PhysicianBasicInfoDTO], page, size, total }`
* `POST api/physicians` (body: `PhysicianOutputDTO` sem `id`) → `201 + Location`
* `PUT api/physicians/{id}` → `204`
* `DELETE api/physicians/{id}` → `204`
* `GET /departments` → `[DepartmentDTO]`
* `GET /specializations` → `[SpecializationDTO]`

**Códigos de erro**

* `400` input inválid(bad request); `401/403` auth(unauthorized); `404` não existe(not found).

**Autorização**

* Leitura: scopes `physicians.read` (Médico/Administrador/Serviço).
* Escrita: scope `physicians.write` (Administrador/Serviço).

---

### 3.2 Consultation Records Service — `/api/v1`

**Entidades expostas**

* `ConsultasRegistoDTO` (input): `{ consultaId, physicianId, patientId, observations, diagnosis?, prescriptions?: [ { drug, dosage, notes? } ] }`
* `ConsultasRegistoOutputDTO` (output): `{ id, createdAt, createdBy, ... + fields de input }`

**Endpoints**

* `POST /records` → cria registo. Valida existência de `physicianId` (Physician) e `patientId` (Patient) e estado da consulta em `Consultations` (`CONFIRMED` ou `DONE`). → `201`
* `GET /records/{id}` → `ConsultasRegistoOutputDTO`
* `GET /records` (query: `consultaId?`, `patientId?`, `physicianId?`, `page?`, `size?`) → paginação
* `PUT /records/{id}` → edições limitadas (correção de texto, anexos). `409` se consulta foi **CANCELLED**.
* `GET /records/export?patientId=` → ficheiro (CSV/JSON) com os registos do paciente.

**Códigos de erro**

* `400` input; `401/403` auth; `404` registo inexistente; `422` referência cruzada inválida (ex.: `physicianId` não encontrado).

**Autorização**

* Criar/editar: scopes `records.write` (Médico) + verificação de **mesmo `physicianId`** do token ou role `ADMIN`.
* Ler: `records.read` (Médico/Admin). Paciente pode ler **apenas os seus** via gateway (token do paciente).

---

## 4) Comunicação entre serviços

**Chamadas síncronas necessárias**

* Records → Physician: `GET /physicians/{id}` (validação). Cache de 5 min para `200/404`.
* Records → Patient: `GET /patients/{id}` (validação básica).
* Records → Consultations: `GET /consultas/{id}` (confirmar estado da consulta).

**Timeouts & resiliência**

* Timeout de cliente: 800 ms.
* Retry: 1 tentativa em `5xx` transitório.
* Circuit breaker: abre a 50% falhas/20 pedidos; fecha após 30 s.

---

## 5) Fan‑out entre réplicas (requisito do trabalho)

* Em cada serviço, manter lista de **peers** (ex.: `application.yml` ➜ `service.peers=[http://svc-1:8080, http://svc-2:8080, …]`).
* Quando um **GET** local devolve vazio, propagar em **paralelo** a pesquisa aos peers e **agregar** a **primeira** resposta válida.
* Cache local curta (Caffeine 60 s) para a chave pedida.
* Responder `404` se nenhum peer possuir o recurso.

**Exemplos de rotas com fan‑out**

* Physician: `GET /physicians/{id}`
* Records: `GET /records/{id}`, `GET /records?consultaId=`

---

## 6) Segurança

* **Autenticação**: JWT assinado pelo **Identity**. Cada serviço valida assinatura com **chave privada**(gerada como token).
* **Scopes** por endpoint (acima). Roles mapeadas: `ADMIN`, `DOCTOR`, `PATIENT`, `SERVICE`.
* **Auditoria** (todos os serviços): guardar `{whenUTC, sub, scope, action, resourceType, resourceId, status}`.

---

### Anexo A — Exemplos JSON

**PhysicianOutputDTO**

```json
{
  "id": "1",
  "fullname": "Dra. Ana Silva",
  "email": "ana.silva@clinica.pt",
  "department": "departamento de cardiologia",
  "speciality": "cardiologia",
  "phoneNumber": "+351-910000000",
  "adress":"Rua das Flores, Porto, nº12",
  "workStartTime": "9:00",
  "workEndTime": "17:00",
  "optionalDescription":"Especialista na área cardiovascular."
}
```

**ConsultasRegistoDTO (input)**

```json
{
  "consultaId": "1",
  "consultaregistoid": "1",
  "treatmentRecommendations": "Recomendado repouso prolongado",
  "diagnosis": "Hipertensão controlada",
  "prescriptions":"Losartan 50mg",
  "createdAt":"19/07/2025"
}
```

---