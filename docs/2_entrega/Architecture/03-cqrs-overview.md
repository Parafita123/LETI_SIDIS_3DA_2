# 03 – CQRS Design (Physicians, Patients, Scheduling, Clinical Records)

## 0. Objetivo do documento

Este documento descreve como o padrão **CQRS (Command–Query Responsibility Segregation)** é aplicado aos recursos principais do sistema HAP na Parte 2 do projeto SIDIS:

- **Doctors** (Physician Service)
- **Patients** (Patient Service)
- **Consultations** (Scheduling Service)
- **Consultation Notes** (Clinical Records Service)

E como isso se articula com:

- **AMQP + Message Broker** (eventos de domínio)
- **Database-per-Service / per microservice instance**
- **Fan-out entre réplicas** (requisito do trabalho, já descrito no README)

O foco é **Design** (não código), servindo de ponte entre o README (contratos REST) e os documentos de deployment e messaging.

---

## 1. Visão geral de CQRS no projeto

### 1.1 Motivação (ligação ao enunciado SIDIS)

O enunciado da Assignment 2 exige explicitamente a adoção de CQRS para os recursos Doctors, Clients, Consultations e Consultation Notes.

No estado atual (Parte 1):

- Os serviços expõem endpoints REST que **misturam leitura e escrita** sobre o mesmo modelo de dados e a mesma base de dados.

Na Parte 2 passamos para um modelo em que:

- **Comandos (commands)** são operações que **alteram estado** (criar, atualizar, cancelar, etc.).
- **Consultas (queries)** são operações **apenas de leitura**, otimizadas para busca e exploração de dados.
- Internamente, comandos e queries podem usar **modelos e stores distintos** (write vs read model).

### 1.2 Estratégia adotada

Em vez de separar em microserviços diferentes de “Command Service” e “Query Service”, adotamos uma abordagem **pragmática**:

- Cada domínio (Patients, Physicians, Consultations, Records) continua num **microserviço único** (bounded context).
- Dentro de cada serviço:
    - Temos **camadas/handlers distintos** para Commands e Queries.
    - Mantemos um **write model** (modelo “completo” de domínio, focado em invariantes).
    - Introduzimos **read models** mais simples, ajustados às queries mais frequentes.
- Para cumprir o requisito de **database-per-microservice-instance**:
    - Cada instância tem **o seu próprio read model**, armazenado numa base de dados local (ou schema separado).
    - O write model pode ser partilhado por serviço ou, pelo menos, existir numa instância “líder” – detalhado no documento de data management.

### 1.3 Relação com eventos, broker e fan-out

- Cada comando bem-sucedido **publica um evento de domínio** no message broker (AMQP).
- Os **read models locais** de cada instância são atualizados consumindo esses eventos (eventual consistency).
- O requisito de **fan-out entre réplicas** (quando o GET local não encontra o recurso, consulta os peers) encaixa assim:
    - O caminho **normal** da query é o **read model local**.
    - Se não existir localmente, a instância pode:
        - Consultar peers via HTTP (modelo do README).
        - Ou, alternativamente, apontar para uma instância que tenha o read model mais completo (dependendo do deployment final).

---

## 2. Comandos e Queries por serviço

Nesta secção listamos, para cada serviço principal, os **Commands** e **Queries** identificados, e como se mapeiam para os endpoints REST existentes e/ou novos.

> Nota: os endpoints externos continuam HTTP/REST, conforme o enunciado. A separação CQRS é sobretudo **lógica/arquitectural**, podendo ou não refletir-se em rotas diferentes.

---

### 2.1 Physician Service (Doctors)

Os contratos atuais do Physician Service já estão descritos no README.

#### 2.1.1 Commands (alteram estado)

Operações:

1. **RegisterPhysician**
    - Endpoint REST:
        - `POST /api/v1/physicians` (body: `PhysicianOutputDTO` sem `id`)
    - Responsabilidade:
        - Criar um novo médico com dados completos (nome, email, departamento, especializações, etc.).
    - Efeitos:
        - Escreve no **write model** `Physician`.
        - Publica evento `PhysicianRegistered`.

2. **UpdatePhysician**
    - Endpoint REST:
        - `PUT /api/v1/physicians/{id}` 
    - Responsabilidade:
        - Atualizar dados de um médico existente (contactos, departamento, descrição).
    - Efeitos:
        - Atualiza write model.
        - Publica `PhysicianUpdated`.

3. **DeletePhysician** (se permitido pelas regras de negócio)
    - Endpoint REST:
        - `DELETE /api/v1/physicians/{id}` 
    - Responsabilidade:
        - Marcar médico como inativo / remover, conforme política.
    - Efeitos:
        - Atualiza write model (soft delete recomendado).
        - Publica `PhysicianDeactivated`.

#### 2.1.2 Queries (apenas leitura)

1. **GetPhysicianDetails**
    - Endpoint REST:
        - `GET /api/v1/physicians/{id}` > `PhysicianOutputDTO`
    - Fonte de dados:
        - **Read model** local para detalhes de médico (inclui dados enriquecidos prontos para resposta).

2. **SearchPhysicians**
    - Endpoint REST:
        - `GET /api/v1/physicians?name=&departmentId=&specializationId=&page=&size=` > lista de `PhysicianBasicInfoDTO` 
    - Fonte de dados:
        - Read model indexado por nome, departamento, especialização.
    - Integração com fan-out:
        - Se a instância local não tiver o médico, pode consultar peers (fan-out), conforme descrito no README.

3. **ListDepartments / ListSpecializations**
    - Endpoints:
        - `GET /api/v1/departments`
        - `GET /api/v1/specializations`
    - Fonte:
        - Read model global ou replicado, uma vez que são tabelas de referência.

---

### 2.2 Patient Service (Clients)

O README identifica o domínio Patient como dono de Patient e consumidor em Consultations e Records.

#### 2.2.1 Commands

1. **RegisterPatient**
    - Endpoint típico:
        - `POST /api/v1/patients`
    - Responsabilidade:
        - Criar um paciente com os dados obrigatórios (nome, email, data de nascimento, telefone, etc.).
    - Efeitos:
        - Escreve no write model `Patient`.
        - Publica `PatientRegistered`.

2. **UpdatePatientData**
    - Endpoint:
        - `PUT /api/v1/patients/{id}`
    - Responsabilidade:
        - Atualizar dados pessoais (contactos, eventualmente seguro, etc.).
    - Efeitos:
        - Atualiza write model.
        - Publica `PatientUpdated`.

3. **DeactivatePatient** (opcional)
    - Endpoint:
        - `DELETE /api/v1/patients/{id}` ou `PATCH /api/v1/patients/{id}/deactivate`.
    - Efeitos:
        - Marca paciente como inativo.
        - Publica `PatientDeactivated`.

#### 2.2.2 Queries

1. **GetPatientDetails**
    - Endpoint:
        - `GET /api/v1/patients/{id}`
    - Fonte:
        - Read model com informação agregada para visualização (ex.: eventualmente com número de consultas, etc.).

2. **SearchPatientsByName**
    - Endpoint:
        - `GET /api/v1/patients?name=&page=&size=`
    - Fonte:
        - Read model otimizado para pesquisa por texto / nome.

3. **SearchPatientsByContact** (telef./email – extensão de PSOFT Phase 2)
    - Endpoint:
        - `GET /api/v1/patients?email=` ou `?phone=`
    - Fonte:
        - Índices específicos no read model.

---

### 2.3 Scheduling Service (Consultations)

O domínio Consultation (agendamento) é central para a coordenação entre Patient, Physician e Records.

#### 2.3.1 Commands

1. **ScheduleConsultation**
    - Endpoint (exemplo):
        - `POST /api/v1/consultations`
    - Inputs:
        - `patientId`, `physicianId`, `dateTime`, `type`, etc.
    - Responsabilidade:
        - Criar consulta em estado inicial (`CREATED`/`PENDING`) e iniciar SAGA de validação (Patient + Physician).
    - Efeitos:
        - Escreve no write model `Consultation`.
        - Publica `ConsultationSchedulingStarted` (para SAGA).

2. **ConfirmConsultation**
    - Endpoint:
        - `POST /api/v1/consultations/{id}/confirm`
    - Responsabilidade:
        - Mudar estado de `CREATED/PENDING` > `CONFIRMED`, após sucesso da SAGA e validações.
    - Efeitos:
        - Atualiza write model.
        - Publica `ConsultationScheduled`.

3. **CancelConsultation**
    - Endpoint:
        - `POST /api/v1/consultations/{id}/cancel`
    - Responsabilidade:
        - Mudar estado para `CANCELLED`, eventualmente disparando SAGA para atualização de registos clínicos (void dos records).
    - Efeitos:
        - Atualiza write model.
        - Publica `ConsultationCancellationRequested` e, no fim, `ConsultationCancelled`.

4. **UpdateConsultationDetails**
    - Endpoint:
        - `PUT /api/v1/consultations/{id}`
    - Responsabilidade:
        - Ajustar data/hora, tipo de consulta, etc., respeitando regras (por ex., não alterar depois de `DONE`).
    - Efeitos:
        - Atualiza write model.
        - Publica `ConsultationUpdated`.

#### 2.3.2 Queries

1. **GetConsultationById**
    - Endpoint:
        - `GET /api/v1/consultations/{id}`
    - Fonte:
        - Read model local com detalhes (estado, data/hora, dados básicos de paciente/médico, etc.).

2. **ListPatientConsultations**
    - Endpoint:
        - `GET /api/v1/consultations?patientId=&status=&from=&to=&page=&size=`
    - Fonte:
        - Read model indexado por `patientId` + janela temporal, permitindo histórico e listagens futuras/passadas.

3. **ListPhysicianConsultations**
    - Endpoint:
        - `GET /api/v1/consultations?physicianId=&status=&from=&to=&page=&size=`
    - Fonte:
        - Read model otimizado para agenda diária/semanal do médico.

4. **AdminConsultationReports** (estatísticas de Phase 2 PSOFT)
    - Endpoints de query (possivelmente num sub-recurso `/reports`):
        - `GET /api/v1/consultations/reports/top-physicians`
        - `GET /api/v1/consultations/reports/monthly-stats`
    - Fonte:
        - Read model analítico (tabelas pré-agregadas) alimentado por eventos `ConsultationScheduled`, `ConsultationCancelled`, `ConsultationDone`.

---

### 2.4 Clinical Records Service (Consultation Notes)

Já documentado no README com os endpoints `/records`.

#### 2.4.1 Commands

1. **CreateConsultationRecord**
    - Endpoint:
        - `POST /api/v1/records`
    - Inputs:
        - `consultaId`, `physicianId`, `patientId`, `observations`, `diagnosis`, `prescriptions`
    - Responsabilidades:
        - Validar IDs cruzados (via HTTP ou eventos) e estado da consulta (`CONFIRMED`/`DONE`).
        - Criar registo clínico associado a uma consulta.
    - Efeitos:
        - Escreve no write model `ConsultationRecord`.
        - Publica `ConsultationRecordCreated`.

2. **UpdateConsultationRecord**
    - Endpoint:
        - `PUT /api/v1/records/{id}`
    - Responsabilidades:
        - Permitir correções limitadas (texto, anexos) se as regras de negócio permitirem.
        - Rejeitar (`409`) se a consulta estiver `CANCELLED`.
    - Efeitos:
        - Atualiza write model.
        - Publica `ConsultationRecordUpdated`.

3. **VoidRecordsForCancelledConsultation** (participação na SAGA de cancelamento)
    - Não necessariamente exposto como endpoint externo; pode ser acionado por evento `ConsultationCancellationRequested`.
    - Responsabilidade:
        - Marcar registos associados à consulta como `VOIDED`/`CANCELLED`.
    - Efeitos:
        - Atualiza write model.
        - Publica `ConsultationRecordsVoided`.

#### 2.4.2 Queries

1. **GetRecordById**
    - Endpoint:
        - `GET /api/v1/records/{id}` > `ConsultasRegistoOutputDTO` 
    - Fonte:
        - Read model local para records.

2. **SearchRecords**
    - Endpoint:
        - `GET /api/v1/records?consultaId=&patientId=&physicianId=&page=&size=`
    - Fonte:
        - Read model com índices por `consultaId`, `patientId`, `physicianId`.
    - Integração com fan-out:
        - Exemplos no README indicam que `GET /records/{id}` e `GET /records?consultaId=` podem usar fan-out para peers.

3. **ExportPatientRecords**
    - Endpoint:
        - `GET /api/v1/records/export?patientId=` (CSV/JSON)
    - Fonte:
        - Read model preparado para export (pode até ser uma vista/denormalização específica).

---

## 3. Modelos de escrita (write models) vs. leitura (read models)

### 3.1 Write model por serviço

Em cada microserviço:

- O write model mantém a **forma canónica** das entidades de domínio:
    - `Physician`, `Patient`, `Consultation`, `ConsultationRecord`.
- Write models são responsáveis por:
    - Invariantes de negócio (ex.: **não se pode criar record se a consulta está CANCELLED**).
    - Transições de estado (ex.: `CREATED` > `CONFIRMED` > `DONE` > etc.).
- Cada comando:
    - Valida input.
    - Aplica regras de negócio via entidades/aggregates.
    - Persiste as alterações na DB do serviço.
    - Publica um evento no broker.

### 3.2 Read models por instância (database-per-instance)

Para cumprir **CQRS** e **database-per-microservice-instance**:

- Cada instância de um microserviço mantém **um read model próprio** (por DB ou schema separado), preenchido por:
    - Eventos do próprio serviço (ex.: `PhysicianRegistered`, `PhysicianUpdated`).
    - Eventos de outros serviços (quando for relevante para queries agregadas ou Sagas).
- Exemplos:
    - `physician-service-instance-1`:
        - `physicians_read_db_1` > tabelas para listagens, filtros, etc.
    - `physician-service-instance-2`:
        - `physicians_read_db_2` > réplica lógica, alimentada pelos mesmos eventos.
- Em caso de falha de uma instância/DB de leitura, as outras instâncias continuam a servir queries com o seu próprio read model (consistência eventual).

---

## 4. Fluxos CQRS end-to-end (exemplos)

### 4.1 Fluxo 1 – Agendar consulta e listar consultas de um paciente

1. **Command:** `ScheduleConsultation` (POST `/consultations`).
    - Scheduling escreve consulta em estado `PENDING` no write model.
    - Publica `ConsultationSchedulingStarted`.

2. **SAGA / Validações (fora do escopo deste doc, mas referenciado):**
    - Patient e Physician validam os IDs e disponibilidade com transações locais.
    - Scheduling, como orchestrator, muda a consulta para `CONFIRMED` e publica `ConsultationScheduled`.

3. **Atualização de read models:**
    - Cada instância do Scheduling Service recebe `ConsultationScheduled`.
    - Atualiza o seu read model (`consultations_read_db_i`) com registos prontos para queries (por paciente, por médico, etc.).

4. **Query:** `ListPatientConsultations` (GET `/consultations?patientId=...`).
    - O endpoint lê apenas do read model local.
    - Se a instância ainda não tiver recebido o evento (lag), o resultado pode estar temporariamente desatualizado – **consistência eventual**.

### 4.2 Fluxo 2 – Criar registo clínico e obter registos por paciente

1. **Command:** `CreateConsultationRecord` (POST `/records`).
    - Clinical Records valida IDs e estado de consulta.
    - Escreve o novo record no write model.
    - Publica `ConsultationRecordCreated`.

2. **Atualização de read models:**
    - As instâncias do Clinical Records Service recebem o evento e atualizam:
        - Índices por `patientId`, `physicianId`, `consultaId`.
        - Eventualmente, um read model para export.

3. **Query:** `SearchRecords` / `ExportPatientRecords`.
    - Apenas leem do read model local.
    - Fan-out entre réplicas pode ser usado para `GET /records/{id}` e pesquisas por consulta, conforme README.

---

## 5. Integração CQRS com fan-out entre réplicas

O README define explicitamente o comportamento de fan-out para algumas rotas de GET:

- O serviço mantém uma lista de peers (`service.peers=[http://svc-1:8080, ...]`).
- Se a query local não encontra o recurso, procura nos peers.
- Usa cache local curta para os resultados (ex.: 60s).

No contexto de CQRS:

- As queries **levam sempre o read model local como fonte principal**.
- O fan-out é um mecanismo de **fallback** quando:
    - O read model local ainda não recebeu o evento (lag).
    - Ou a distribuição de dados é tal que certos recursos só existem em algumas instâncias.

Isto permite:

- Cumprir o requisito do trabalho.48]{index=48}
- Manter a separação commands/queries, visto que fan-out só afeta queries.

---

## 6. Resumo

Neste documento, definimos:

- **Commands e Queries** para os quatro recursos-chave exigidos pelo enunciado (Doctors, Patients, Consultations, Consultation Notes).
- A forma como estes comandos/queries mapeiam para os **endpoints REST** já definidos no README.
- A distinção entre **write models** (focados em regras de negócio e invariantes) e **read models por instância**, que cumprem simultaneamente CQRS e o requisito de **database-per-microservice-instance**.
- Dois **fluxos CQRS end-to-end**, que mais tarde serão reutilizados nos documentos de:
    - **Sagas e DDD** (coordenação entre serviços).
    - **Messaging/Broker** (eventos AMQP).
    - **Deployment multi-instância**.

Os próximos documentos a partir deste são:

- `04-messaging-and-broker.md` – onde se detalham os eventos, tópicos/filas e a integração com AMQP.
- `05-deployment-multi-instance.md` – onde se explica como levantar múltiplas instâncias e respetivos read models.
