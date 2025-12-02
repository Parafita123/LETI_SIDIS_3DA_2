# 01 – Current Architecture (C4+1)

## 0. Scope & Purpose

Este documento descreve a **arquitectura atual** do sistema desenvolvido na fase correspondente à primeira parte do projeto.

Foca-se em:

- Identificar os principais **atores** e **sistemas externos** (C1 – Context).
- Descrever os **containers/microserviços** e as suas responsabilidades (C2 – Container).
- Resumir os **componentes internos** mais relevantes de cada serviço (C3 – Component).
- Apontar algumas decisões estruturais de **código e padrões** (C4 – Code).
- Explicitar a forma atual de **deployment**, ainda com **uma instância por serviço** (+1 – Deployment).

Esta arquitetura inspira-se diretamente no domínio e requisitos definidos no projeto de PSOFT (sistema HAP – Health Appointment Platform) e foi refatorada para um estilo de microserviços na UC de SIDIS.

---

## 1. C1 – System Context

### 1.1 Domínio funcional

O sistema suporta um cenário de **clínica / hospital** com os seguintes domínios principais:

- **Patients** – registo de dados de pacientes.
- **Physicians** – médicos, departamentos e especializações.
- **Consultas (Scheduling)** – agendamento de consultas entre pacientes e médicos.
- **Consultation Records** – registos clínicos associados a consultas (diagnóstico, observações, prescrições).
- **Identidade / Autenticação** – utilizadores, credenciais, JWT, roles/scopes.

### 1.2 Atores principais

- **Paciente (PATIENT)**
    - Regista-se e autentica-se.
    - Marca consultas, consulta histórico de consultas e registos clínicos (apenas os seus, via gateway).

- **Médico (DOCTOR)**
    - Consulta lista de pacientes associados às suas consultas.
    - Cria e edita registos clínicos (consultation records) para as consultas que realiza.

- **Administrador (ADMIN)**
    - Gere médicos, departamentos, especializações.
    - Pode consultar informação agregada e/ou administrativa.

- **Serviços internos (SERVICE role)**
    - Chamadas service-to-service para validações cruzadas (por ex. Records > Physician/Patient/Consultations).

- **Identity Provider / Auth Server (Identity Service)**
    - Emite tokens JWT com scopes e roles.

### 1.3 Sistemas e contexto

Numa visão de contexto:

- O **API Gateway** expõe uma API HTTP/JSON única para os clientes (frontends / Postman / testes automatizados).
- Os **microserviços internos** (Patient, Physician, Scheduling, Clinical Records, Identity) comunicam:
    - Diretamente com as suas **bases de dados privadas** (database-per-service).
    - Entre si por **HTTP síncrono**, através de endpoints REST (ex.: Records a validar IDs em Physician/Patient/Consultations).

Não existe ainda message broker ou replicação de instâncias — isso é alvo da Parte 2 do projeto.

---

## 2. C2 – Container View

Nesta secção descrevemos os **containers lógicos** que compõem o sistema atual.

### 2.1 API Gateway

- **Responsabilidade:**
    - Ponto de entrada único para clientes externos.
    - Encaminha pedidos para os microserviços internos.
    - Aplica autenticação/autorização com base em JWT emitidos pelo Identity Service.

- **Tecnologia:**
    - Serviço HTTP (Java/Spring ou tecnologia equivalente) com mapeamento de rotas / filtros.
    - Integração com Identity Service para validação de tokens.

---

### 2.2 Identity Service

- **Responsabilidade:**:
    - Gestão de utilizadores, credenciais e roles (`ADMIN`, `DOCTOR`, `PATIENT`, `SERVICE`).
    - Emissão de **tokens JWT** com scopes (ex.: `physicians.read`, `records.write`, etc.).
    - Exposição da chave pública/metadata necessária para validação dos tokens pelos restantes serviços.

- **Base de dados própria:**
    - Tabela(s) de utilizadores, roles, tokens revogados, etc.
    - Não é lida diretamente por outros serviços (apenas via chamadas HTTP/validação de tokens).

---

### 2.3 Patient Service

- **Responsabilidade:**:
    - CRUD completo de **Patient** (registos de pacientes).
    - Disponibilizar detalhes de pacientes a outros serviços (Scheduling, Clinical Records) via HTTP.
    - Garantir que as regras de negócio do domínio Patient não são replicadas noutros serviços.

- **Base de dados própria:**
    - Tabelas de `patients` (e eventuais tabelas auxiliares específicas deste domínio).
    - IDs expostos como `PatientId` (UUID/String).

- **Consumidores típicos:**
    - Scheduling Service (para validar pacientes em marcação de consulta).
    - Clinical Records Service (para associar registos clínicos a um paciente).

---

### 2.4 Physician Service

- **Responsabilidade:**:
    - Gestão de **Physician** (médicos) com informação detalhada: nome, email, telefone, departamento, especializações, sala, horário de trabalho, etc.
    - Gestão de **Department** e **Specialization** como tabelas de referência.
    - Exposição de:
        - `PhysicianOutputDTO` – informação detalhada.
        - `PhysicianBasicInfoDTO` – informação básica para listagens.
    - Endpoints para pesquisa de médicos com filtros (nome, departamento, especialização).

- **Base de dados própria:**
    - Tabelas `physicians`, `departments`, `specializations`.
    - Exposição de IDs como `PhysicianId`.

- **Consumidores típicos:**
    - Scheduling Service – para verificar se o médico existe e está ativo.
    - Clinical Records Service – para validar `physicianId` ao criar registos.

---

### 2.5 Scheduling Service (Consultations)

- **Responsabilidade:**:
    - Gestão de **Consultas** (agendamentos) entre pacientes e médicos.
    - Estados da consulta: `CREATED`, `CONFIRMED`, `CANCELLED`, `DONE`.
    - Lógica principal:
        - Criar consulta para um `patientId` e `physicianId` válidos.
        - Gerir alterações de estado (confirmar, cancelar, marcar como realizada).
        - Exposição de listagens (consultas futuras, passadas, por paciente/médico).

- **Base de dados própria:**
    - Tabela `consultations` (ou equivalente) com referências externas (`patientId`, `physicianId`) guardadas como **IDs imutáveis**, não chaves estrangeiras físicas para outras DBs.

- **Consumidores típicos:**
    - Clinical Records Service (para verificar o estado da consulta antes de criar/editar registo clínico).

---

### 2.6 Clinical Records Service

- **Responsabilidade:**:
    - Gestão de **registos clínicos** (`ConsultasRegisto` / `ConsultationRecord`) associados a uma consulta.
    - Entidades expostas:
        - `ConsultasRegistoDTO` (input) – inclui `consultaId`, `physicianId`, `patientId`, `observations`, `diagnosis`, `prescriptions`, etc.
        - `ConsultasRegistoOutputDTO` (output) – adiciona metadados como `id`, `createdAt`, `createdBy`.
    - Endpoints atuais:
        - `POST /records` – cria registo (valida existência de `physicianId`, `patientId` e estado da consulta em Scheduling – apenas `CONFIRMED` e `DONE`).
        - `GET /records/{id}` – obtém um registo.
        - `GET /records` – pesquisa por `consultaId`, `patientId`, `physicianId` com paginação.
        - `PUT /records/{id}` – edições limitadas (corrigir texto, anexos); erro `409` se a consulta foi `CANCELLED`.
        - `GET /records/export?patientId=` – exporta registos de um paciente (CSV/JSON).

- **Base de dados própria:**
    - Tabela de `records` (registos clínicos) com referências para `consultaId`, `physicianId`, `patientId` como IDs.

- **Dependências sincronas via HTTP:**:
    - Records > Physician: valida `physicianId` via `GET /physicians/{id}`.
    - Records > Patient: valida `patientId` via `GET /patients/{id}`.
    - Records > Consultations: valida `consultaId` e estado via `GET /consultas/{id}`.

- **Resiliência configurada:**:
    - Timeout cliente: 800 ms.
    - Retry: 1 tentativa em erros `5xx` transitórios.
    - Circuit breaker: abre com 50% falhas em 20 pedidos; volta a fechar após 30 s.

---

### 2.7 Regras transversais entre serviços

Tal como definido no README de raiz:

- Nenhum serviço lê diretamente a **base de dados de outro serviço**.
- Referências cruzadas são sempre **IDs imutáveis** guardados como campos simples (`physicianId`, `patientId`, `consultaId`, ...).
- Qualquer validação cruzada faz-se por **HTTP síncrono**.
- Segurança baseada em **JWT + scopes**:
    - Exemplo:
        - `physicians.read`/`physicians.write` para Physician Service.
        - `records.read`/`records.write` para Clinical Records Service.

---

## 3. C3 – Component View (por serviço)

Aqui damos uma visão genérica dos componentes internos comuns a todos os serviços (Patient, Physician, Scheduling, Clinical Records, Identity).

### 3.1 Componentes típicos de um microserviço

Cada microserviço segue uma estrutura semelhante:

- **API Layer / Controllers**
    - Endpoints REST (por ex. `GET /physicians/{id}`, `POST /records`).
    - Tratam de:
        - Mapeamento HTTP ↔ DTO.
        - Validação básica de input.
        - Autenticação/autorização com base no JWT (scopes/roles).

- **Application / Service Layer**
    - Implementa a **lógica de caso de uso**:
        - Criar paciente, criar médico, agendar consulta, criar/editar registo clínico, etc.
    - Orquestra chamadas a outros microserviços quando necessário (via HTTP).

- **Domain / Model Layer**
    - Entidades de domínio (Patient, Physician, Consultation, ConsultationRecord).
    - Regras de negócio locais (por ex. estados permitidos de consulta, regras simples de validação).

- **Persistence / Repository Layer**
    - Implementação de acesso a dados (typicamente via JPA/ORM).
    - Cada serviço apenas acede à **sua** base de dados.

- **Infra / Cross-cutting**
    - Configuração de comunicação HTTP com outros serviços (clients).
    - Configuração de autenticação JWT.
    - Configuração de logging, métricas, etc.

### 3.2 Componentes específicos relevantes

- **Clinical Records Service**
    - Componente de **validação externa** que encapsula chamadas a Physician, Patient e Scheduling, aplicando os timeouts, retries e circuit breaker configurados.

- **Physician Service**
    - Componente de pesquisa paginada e filtrada (`name`, `departmentId`, `specializationId`).

- **Identity Service**
    - Componente de emissão e verificação de tokens, gestão de roles e scopes.

---

## 4. C4 – Code View (high-level)

> Nota: aqui não listamos ficheiros concretos mas sim padrões e decisões de código.

- **Linguagem & Frameworks**
    - Backend implementado em **Java**, com framework de aplicação web (tipo Spring Boot/Spring Web) e camada de persistência (tipo JPA/ORM), alinhado com o sistema original de PSOFT (Spring Boot + JPA).

- **Estilo de API**
    - Endpoints RESTful com JSON (`camelCase` nas propriedades).
    - Uso consistente de códigos de erro HTTP (`400`, `401`, `403`, `404`, `409`, `422`, `5xx`).

- **IDs e datas**
    - IDs de recursos (`PhysicianId`, `PatientId`, `ConsultaId`, `RecordId`) são `String` (UUID).
    - Timestamps em formato ISO-8601 UTC (`2025-10-15T13:45:00Z`).

- **Segurança**
    - Implementação de filtros/intercetores para validar tokens JWT em cada serviço.
    - Controlos de autorização por scope/role ao nível dos endpoints (ex.: `records.write` apenas para médicos/administradores).

---

## 5. +1 – Deployment View (estado atual)

### 5.1 Ambiente típico de desenvolvimento

Na Parte 1, o deployment é pensado para um ambiente de desenvolvimento/local:

- **Instâncias:**
    - 2 instância de cada microserviço:
        - `api-gateway`
        - `identity-service`
        - `patient-service`
        - `physician-service`
        - `scheduling-service`
        - `clinical-records-service`
    - Cada serviço exposto numa porta HTTP distinta.

- **Bases de dados:**
    - 1 base de dados por serviço (database-per-service).
    - Todas a base de dados, exceto uma, são H2, como extensão do Spring Boot.
    - Cada DB isolada; não há triggers, views nem acessos cruzados entre DBs.

- **Comunicação:**
    - HTTP/JSON entre serviços (síncrono).
    - Não existe ainda message broker ou filas; não há replicação de instâncias.

### 5.2 Requisitos futuros (ponte para Parte 2 – apenas para contexto)

> **Importante:** Não está implementado na arquitetura atual, mas o README do projeto já antecipa alguns requisitos de **fan-out entre réplicas** e maior resiliência:

- Manter lista de peers por serviço, para fan-out de leituras quando a instância local não tem o recurso.
- Aplicar cache local curta para resultados de peers.

Estes pontos serão detalhados na documentação da **nova arquitetura** (com CQRS, AMQP e múltiplas instâncias) e não fazem parte do escopo deste documento, que se foca na situação **atual**.

---
