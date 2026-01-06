# 05 – Security

## 0. Objetivo

Este documento descreve a implementação de **segurança** no sistema SIDIS/HAP, conforme os requisitos da Parte 3 (P3).

A estratégia de segurança cobre dois eixos principais:

- **User-to-Service Security**: autenticação e autorização de utilizadores finais (paciente, médico, administrador).
- **Service-to-Service Security**: proteção das comunicações internas entre microserviços.

O objetivo é garantir:
- acesso controlado a dados sensíveis (ex.: dados clínicos),
- aplicação consistente de regras de autorização,
- alinhamento com princípios de **zero-trust** e boas práticas de sistemas distribuídos.

---

## 1. Segurança User-to-Service

### 1.1 Autenticação

A autenticação de utilizadores é realizada através do **Identity Service**, que atua como **Authorization Server**.

Fluxo típico:
1. O utilizador autentica-se (login).
2. O Identity Service valida as credenciais.
3. É emitido um **JWT** assinado.
4. O token é enviado pelo cliente em cada request (`Authorization: Bearer <token>`).

O API Gateway e/ou os serviços internos validam o token em cada pedido.

---

### 1.2 JWT e OAuth2

Os tokens JWT incluem:
- `sub` (identidade do utilizador)
- `roles` (ex.: ADMIN, DOCTOR, PATIENT, SERVICE)
- `scopes` (ex.: consultations.write, records.read)
- `exp` (expiração)
- claims adicionais relevantes para autorização

O modelo segue os princípios do **OAuth2**, separando:
- autenticação (quem é o utilizador)
- autorização (o que pode fazer)

---

### 1.3 Autorização baseada em roles e scopes

A autorização é aplicada:
- no API Gateway (primeira linha de defesa),
- e reforçada nos serviços internos.

Exemplos:
- Endpoints de escrita exigem scopes `*.write`.
- Endpoints de leitura exigem scopes `*.read`.
- Alguns endpoints exigem simultaneamente role + scope.

---

## 2. Regras de autorização por serviço

### 2.1 Scheduling Service (Consultas)

- **Criar consulta**
    - Roles permitidas: PATIENT, ADMIN
    - Scope: consultations.write

- **Confirmar / cancelar consulta**
    - Roles permitidas: PATIENT (própria consulta), ADMIN
    - Scope: consultations.write

- **Consultar consultas**
    - PATIENT: apenas as suas
    - DOCTOR: consultas onde participa
    - ADMIN: todas
    - Scope: consultations.read

---

### 2.2 Clinical Records Service

- **Criar registo clínico**
    - Roles permitidas: DOCTOR, ADMIN
    - Scope: records.write
    - Regras adicionais:
        - consulta válida
        - consulta associada ao médico

- **Editar registo clínico**
    - Roles permitidas: DOCTOR (autor), ADMIN
    - Scope: records.write

- **Consultar registos**
    - PATIENT: apenas os seus
    - DOCTOR: apenas das suas consultas
    - ADMIN: todos
    - Scope: records.read

---

### 2.3 Patient Service

- **Criar / editar paciente**
    - Roles permitidas: ADMIN
    - Scope: patients.write

- **Consultar dados de paciente**
    - PATIENT: apenas os seus dados
    - DOCTOR / SERVICE: apenas quando necessário para validação
    - Scope: patients.read

---

### 2.4 Physician Service

- **Criar / editar médico**
    - Roles permitidas: ADMIN
    - Scope: physicians.write

- **Consultar médicos**
    - Roles permitidas: PATIENT, DOCTOR, ADMIN
    - Scope: physicians.read

---

### 2.5 Identity Service

- **Login / token**
    - Público (sem autenticação prévia)

- **Gestão de utilizadores**
    - Roles permitidas: ADMIN
    - Scopes administrativos específicos

---

## 3. Segurança Service-to-Service

### 3.1 Validação de token em chamadas internas

Chamadas entre microserviços:
- incluem sempre um JWT válido,
- usam role `SERVICE`,
- incluem scopes mínimos necessários.

Cada serviço:
- valida assinatura do token,
- valida expiração,
- valida claims relevantes.

---

### 3.2 mTLS (Mutual TLS)

Para reforçar a segurança interna, é aplicado **mTLS** em chamadas service-to-service.

Características:
- cada serviço possui certificado próprio,
- truststore partilhada com certificados válidos,
- chamadas falham se o certificado não for reconhecido.

Benefícios:
- autenticação mútua entre serviços,
- proteção contra chamadas internas não autorizadas,
- alinhamento com zero-trust.

---

### 3.3 Segurança na mensageria (RabbitMQ)

- Ligações ao broker protegidas por credenciais.
- Permissões por exchange/queue.
- Mensagens incluem metadata de segurança:
    - correlationId
    - traceId
    - (quando aplicável) sagaId

Consumers validam:
- origem da mensagem,
- formato esperado,
- idempotência.

---

## 4. Gestão de secrets

### 4.1 Princípios

- Secrets **não** são hardcoded.
- Secrets **não** estão no repositório.
- Configuração depende do ambiente (dev, test, prod).

---

### 4.2 Kubernetes Secrets / Environment Variables

Secrets geridos via:
- Kubernetes Secrets
- Environment variables

Exemplos de secrets:
- chaves privadas de JWT
- certificados mTLS
- credenciais de base de dados
- credenciais do RabbitMQ

Os serviços leem secrets no arranque e nunca os expõem em logs.

---

## 5. Como validar a segurança

### 5.1 User-to-Service

- Request sem token → **401 Unauthorized**
- Token inválido ou expirado → **401 Unauthorized**
- Token válido sem scope adequado → **403 Forbidden**
- Token válido com role/scopes corretos → **200 / 201**

---

### 5.2 Service-to-Service

- Chamada sem token SERVICE → **401**
- Chamada com token mas sem scope correto → **403**
- Chamada sem certificado mTLS válido → falha de handshake
- Chamada válida → sucesso

---

### 5.3 Mensageria

- Consumer rejeita mensagens malformadas.
- Mensagens duplicadas não causam efeitos colaterais.
- Falhas são registadas e observáveis.

---