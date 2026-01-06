# 01 – Current Architecture (C4+1)

## 0. Scope & Purpose

Este documento descreve a **arquitetura atual** do sistema, no estado correspondente ao final da **Parte 2 (P2)**.

O objetivo é documentar **o que já está implementado** e suportado pela solução atual, alinhado com os requisitos da P2:

- Adoção de **CQRS** para os recursos principais.
- Adoção de **AMQP + Message Broker** para comunicação assíncrona entre aplicações.
- Manutenção de **endpoints externos HTTP/REST**.
- Adoção de **Database-per-Service**, mais especificamente **database-per-microservice instance**.
- Execução de **pelo menos duas instâncias por microserviço**.
- Existência de **testes de integração** (Postman ou equivalente).

A arquitetura inspira-se no domínio original do projeto HAP (PSOFT) e foi evoluída para um estilo microserviços com padrões avançados de sistemas distribuídos.

---

## 1. C1 – System Context

### 1.1 Domínio funcional

O sistema suporta um cenário de **clínica/hospital**, com os seguintes domínios:

- **Patients** – registo e gestão de pacientes.
- **Physicians** – gestão de médicos, departamentos e especializações.
- **Scheduling / Consultations** – agendamento e gestão do ciclo de vida de consultas.
- **Clinical Records / Consultation Notes** – registos clínicos associados a consultas.
- **Identity / Authentication** – utilizadores, autenticação, JWT e autorização.

### 1.2 Atores principais

- **Paciente (PATIENT)**
    - Regista-se e autentica-se.
    - Cria e consulta consultas e os seus registos associados.

- **Médico (DOCTOR)**
    - Consulta informação necessária para as consultas que realiza.
    - Cria/edita registos clínicos para consultas válidas.

- **Administrador (ADMIN)**
    - Gere médicos, departamentos, especializações.
    - Acede a informação administrativa e agregada.

- **Serviços internos (SERVICE role)**
    - Operações internas service-to-service (quando aplicável), mantendo isolamento de dados.

- **Identity Provider / Auth Server**
    - Emite tokens JWT com roles/scopes.

### 1.3 Sistemas e comunicação

Visão de contexto (alto nível):

- O **API Gateway** expõe uma API única **HTTP/REST** para clientes externos.
- Os **microserviços internos** comunicam:
    - Com as suas **bases de dados privadas** (isolamento por instância).
    - Entre si por:
        - **HTTP síncrono** (apenas quando necessário, e preferencialmente encapsulado em clientes dedicados).
        - **AMQP assíncrono via Message Broker** (eventos) para integração e atualização de read-models, suportando consistência eventual.

---

## 2. C2 – Container View

Nesta secção descrevemos os containers lógicos do sistema.

### 2.1 API Gateway

- **Responsabilidades**
    - Ponto de entrada único para clientes.
    - Encaminhamento por rotas para os serviços internos.
    - Aplicação de autenticação/autorização (JWT) nos endpoints expostos.

- **Tecnologia**
    - Serviço HTTP/REST (Java/Spring ou equivalente).
    - Integração com Identity Service para validação de tokens.

> Nota: o requisito de P2 mantém os endpoints externos como HTTP/REST.

---

### 2.2 Identity Service

- **Responsabilidades**
    - Gestão de utilizadores, credenciais, roles (`ADMIN`, `DOCTOR`, `PATIENT`, `SERVICE`).
    - Emissão de tokens JWT com scopes.

- **Base de dados**
    - Privada e isolada por instância.
    - Não é acedida diretamente por outros serviços.

---

### 2.3 Patient Service (CQRS)

- **Responsabilidades**
    - Operações de escrita (commands) sobre pacientes.
    - Exposição de endpoints de leitura (queries) com foco em performance e resposta rápida.

- **CQRS (visão)**
    - **Write model**: validações e persistência do domínio Patient.
    - **Read model**: projeções / vistas otimizadas para consultas (dependendo dos casos de uso implementados).

- **Base de dados**
    - Uma base por instância (db-per-instance).

---

### 2.4 Physician Service (CQRS)

- **Responsabilidades**
    - Gestão de médicos, departamentos e especializações.
    - Pesquisa por filtros (nome, departamento, especialização, etc.).

- **CQRS (visão)**
    - **Commands** para criação/atualização de Physicians.
    - **Queries** para pesquisa/listagens.

- **Base de dados**
    - Uma base por instância (db-per-instance).

---

### 2.5 Scheduling Service / Consultations (CQRS)

- **Responsabilidades**
    - Gestão do ciclo de vida de consultas.
    - Estados típicos: `CREATED`, `CONFIRMED`, `CANCELLED`, `DONE`.
    - Listagens por paciente/médico/período.

- **CQRS (visão)**
    - **Write side**: criação e transições de estado da consulta.
    - **Read side**: read-models e queries para listagens e estatísticas.

- **Base de dados**
    - Uma base por instância (db-per-instance).

---

### 2.6 Clinical Records / Consultation Notes (CQRS)

- **Responsabilidades**
    - Gestão de registos clínicos associados a consultas.
    - Regras típicas:
        - Apenas certas roles podem criar/editar.
        - Validações com base em estado da consulta.

- **CQRS (visão)**
    - **Commands** para criar/editar registos.
    - **Queries** para obter registos por consulta/paciente/médico, com paginação.

- **Base de dados**
    - Uma base por instância (db-per-instance).

---

### 2.7 Message Broker (AMQP / RabbitMQ)

- **Responsabilidades**
    - Canal de integração assíncrona entre microserviços.
    - Transporte de eventos (publish/subscribe) usados para:
        - Atualização de read-models.
        - Sincronização eventual de dados relevantes entre domínios (sem acesso direto a DBs externas).

- **Propriedades pretendidas**
    - Comunicação assíncrona (reduz acoplamento temporal).
    - Suporte a consistência eventual.
    - Melhor resiliência quando existem falhas temporárias entre serviços.

---

## 3. C3 – Component View (por serviço)

### 3.1 Estrutura típica (com CQRS)

Cada microserviço segue uma estrutura comum:

- **Controllers (API Layer)**
    - Endpoints REST.
    - Autorização por roles/scopes.
    - DTO mapping e validações básicas.

- **Command Side (Write)**
    - Serviços de aplicação responsáveis por comandos.
    - Persistência no write model (DB da instância).

- **Query Side (Read)**
    - Serviços de consulta sobre read-models / views otimizadas.
    - Respostas rápidas para listagens, pesquisas e relatórios.

- **Messaging Layer (AMQP)**
    - Publicação de eventos após comandos concluídos.
    - Consumo de eventos para atualizar read-models e projeções.

- **Repositories (Persistence)**
    - Acesso apenas à DB local da instância.

---

## 4. C4 – Code View (high-level)

- **Linguagem & framework**
    - Backend em Java com framework web e persistência ORM.

- **Estilo de API**
    - REST + JSON.
    - Códigos HTTP consistentes (400/401/403/404/409/422/5xx).

- **Segurança**
    - JWT emitido pelo Identity Service.
    - Autorização por roles/scopes.

- **CQRS**
    - Separação entre componentes orientados a **commands** (write) e **queries** (read).
    - Read-models atualizados por eventos via AMQP.

- **AMQP**
    - Eventos publicados/consumidos via message broker para integração assíncrona.}

---

## 5. +1 – Deployment View (estado atual)

### 5.1 Instâncias e isolamento de dados

A solução é executada com:

- **Pelo menos 2 instâncias de cada microserviço** (gateway e serviços internos).
- **Database-per-instance**:
    - Cada instância de um microserviço liga-se à sua própria base de dados (isolada).
- **Message Broker** (AMQP) acessível pelas instâncias para publicação/consumo de eventos.

### 5.2 Comunicação

- **Clientes externos → Gateway**: HTTP/REST.
- **Gateway → Serviços internos**: HTTP/REST.
- **Serviço ↔ Serviço (integração e read-models)**: AMQP via broker.

---

## 6. Testes de integração

- Existência de coleção de testes (Postman ou equivalente) cobrindo endpoints principais e fluxos críticos do sistema. 

---
