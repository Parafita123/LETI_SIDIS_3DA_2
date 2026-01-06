# 02 – Part 3 Requirements and Plan

## 0. Scope & Purpose

Este documento traduz os requisitos da **Parte 3 (P3)** em **tarefas concretas**, com **ordem de implementação** e **critérios de aceitação** por tópico.

A P3 pretende reforçar a arquitetura (baseada no que foi feito na P2) com características **production-ready**: observabilidade, resiliência, segurança, automação de deployment e governance, incluindo **Saga pattern** e **event sourcing**.

---

## 1. Requisitos P3 

A P3 exige a implementação de quatro melhorias principais e dois padrões explícitos: 

1. **Observability Integration**
    - Logging + metrics + tracing
    - Health checks com liveness e readiness probes

2. **Resilience and Fault Tolerance**
    - Circuit Breaker, Retry, Timeout, Bulkhead (ex.: Resilience4j)
    - Aplicar a REST e message queues
    - Integrar com observabilidade e com Sagas

3. **Security Implementation**
    - OAuth2 (authorization)
    - JWT (token-based authentication)
    - mTLS (mutual TLS) para chamadas internas
    - Foco em endpoints sensíveis e boas práticas (zero-trust, GDPR-like)

4. **Deployment, CI/CD, and Governance**
    - Docker + Kubernetes
    - Estratégias de rollout (Blue-Green ou Canary)
    - Service catalog (ex.: Backstage)
    - API contracts (OpenAPI) + contract testing (ex.: Pact)
    - SLAs + processo de depreciação de APIs
    - Event sourcing integrado com Saga para auditoria e resiliência

Padrões obrigatórios adicionais:
- **Saga pattern** para transações distribuídas
- **Event sourcing** como stream imutável de eventos para alterações de estado :contentReference[oaicite:2]{index=2}

---

## 2. Plano de execução 

### 2.1 Ordem macro 

1. **Deployment base (Kubernetes + Docker)**  
   (necessário para probes, observabilidade e demo)
2. **Observabilidade**  
   (precisamos de ver o que acontece antes de simular falhas)
3. **Resiliência**  
   (introduzir falhas controladas e ver comportamentos)
4. **Segurança**  
   (OAuth2/JWT e depois mTLS service-to-service)
5. **Saga pattern** (1 caso de uso end-to-end)
6. **Event sourcing** (aplicado ao caso de uso escolhido e ligado à Saga)
7. **Governance (OpenAPI, Pact, SLAs, depreciação, service catalog)**

> Nota: se o tempo for curto, o “service catalog” pode ser uma integração mínima (prova de conceito) mas deve existir evidência na documentação/demo.

---

## 3. Breakdown por tópico: tarefas + critérios de aceitação

## 3.1 Observability Integration

### 3.1.1 Tarefas

**Logging**
1. Padronizar logs estruturados (JSON) em todos os serviços.
2. Adicionar correlation-id (ex.: `X-Correlation-Id`) no gateway e propagar para serviços e mensagens AMQP.
3. Centralizar logs (ex.: ELK/EFK, Fluentd/Fluent Bit).

**Metrics**
4. Expor métricas por serviço (ex.: Prometheus endpoint).
5. Criar dashboards (ex.: Grafana) para:
    - taxa de requests
    - latência (p50/p95/p99)
    - taxa de erro (4xx/5xx)
    - métricas AMQP (consumo, backlog, retries)

**Tracing**
6. Integrar distributed tracing (ex.: Jaeger/Zipkin).
7. Instrumentar:
    - chamadas REST inter-serviços
    - publicação/consumo AMQP
    - passos da Saga (cada step como span)

**Health Checks**
8. Adicionar health endpoints e probes (liveness/readiness) por serviço.
9. Incluir dependências no readiness (DB, broker, etc.).

### 3.1.2 Critérios de aceitação

- A demo mostra logs centralizados com filtro por correlation-id.
- Um fluxo com REST + AMQP aparece como um único trace distribuído (com spans por serviço).
- Existem dashboards com métricas mínimas por serviço.
- Kubernetes usa liveness/readiness para gerir pods (ex.: readiness falha quando DB/broker indisponível).

---

## 3.2 Resilience and Fault Tolerance

### 3.2.1 Tarefas

1. Definir timeouts/retries para REST (por cliente e por endpoint crítico).
2. Aplicar Circuit Breaker em chamadas REST críticas.
3. Aplicar Bulkhead (isolamento) em componentes que possam saturar (ex.: chamadas externas, consumers).
4. Definir políticas para AMQP:
    - retry com backoff
    - DLQ (dead-letter) para mensagens falhadas
    - idempotência no consumer (evitar duplicados)
5. Integrar métricas/alertas com observabilidade:
    - circuit breaker OPEN
    - número de retries
    - mensagens em DLQ
6. Integrar resiliência na Saga:
    - se um step falhar, executar compensação
    - tratar timeouts/partições de rede como falhas recuperáveis

### 3.2.2 Critérios de aceitação

- Ao desligar um serviço, o sistema **degrada graciosamente** (não “cai em cascata”).
- Circuit breakers abrem/fecham de forma observável (métricas + logs).
- Mensagens problemáticas acabam em DLQ com prova (logs + contador).
- Saga completa com sucesso em condições normais e compensa corretamente sob falhas induzidas.

---

## 3.3 Security Implementation

### 3.3.1 Tarefas

**OAuth2 + JWT**
1. Padronizar OAuth2/JWT para autenticação e autorização.
2. Garantir scopes/roles coerentes por endpoint sensível.
3. Reforçar validação do token no gateway e/ou nos serviços (definir abordagem e documentar).

**mTLS interno**
4. Implementar mTLS nas chamadas service-to-service (REST) (certificados, truststore/keystore).
5. (Se aplicável) mTLS também nas ligações ao broker e/ou DB, conforme suportado.

**Hardening / compliance**
6. Aplicar princípio de least privilege:
    - endpoints clínicos só acessíveis por DOCTOR/ADMIN (conforme requisitos internos)
    - dados do paciente só visíveis ao próprio ou entidade autorizada
7. Documentar fluxos de segurança (diagrama):
    - login → token → chamada gateway → chamada interna

### 3.3.2 Critérios de aceitação

- Requests sem token / token inválido são bloqueados consistentemente.
- Um serviço não consegue chamar outro sem certificado válido (mTLS).
- Endpoints sensíveis demonstram enforcement correto de scopes/roles.
- Existe documentação clara de security flow e decisões (inclui justificativa “zero trust”).

---

## 3.4 Saga Pattern (Distributed Transactions)

### 3.4.1 Tarefas

1. Selecionar um caso de uso multi-serviço para demonstrar Saga (exemplo sugerido no enunciado: **booking a consultation**). :contentReference[oaicite:3]{index=3}
2. Definir o desenho da Saga:
    - orchestration (coordenador) **ou** choreography (event-driven)
3. Definir steps e compensações:
    - Step 1: reservar slot / criar consulta
    - Step 2: validar médico/paciente
    - Step 3: confirmar consulta
    - Compensações: cancelar consulta, libertar slot, etc.
4. Implementar persistência do estado da Saga (state machine).
5. Instrumentar tracing/logging por `sagaId`.
6. Criar cenários de falha (simulados) e validar compensações.

### 3.4.2 Critérios de aceitação

- Existe um fluxo end-to-end onde múltiplos serviços participam numa transação distribuída.
- Quando um step falha, a Saga executa compensações e termina num estado consistente.
- Logs e traces permitem acompanhar a Saga do início ao fim.
- A demo mostra pelo menos 1 falha e recuperação.

---

## 3.5 Event Sourcing

### 3.5.1 Tarefas

1. Escolher um agregado/entidade para event sourcing (idealmente o mesmo do caso de uso da Saga).
2. Definir eventos imutáveis (ex.: `ConsultationBooked`, `ConsultationConfirmed`, `ConsultationCancelled`, etc.).
3. Persistir o event stream num event store (pode ser DB dedicada/tabela append-only).
4. Reconstruir estado por replay.
5. Integrar com CQRS:
    - projeções/read-models alimentadas pelos eventos
6. Integrar event sourcing com Saga:
    - usar eventos como audit trail e suporte de recuperação

### 3.5.2 Critérios de aceitação

- O estado do agregado pode ser reconstruído apenas a partir do event stream.
- Eventos são imutáveis (append-only) e incluem metadata (timestamp, correlation-id, sagaId).
- Existe prova de auditoria: histórico completo de mudanças.
- Read-model é atualizado via eventos e converge corretamente.

---

## 3.6 Deployment, CI/CD e Governance

### 3.6.1 Tarefas

**Containerização + Kubernetes**
1. Garantir Dockerfiles consistentes para todos os serviços.
2. Criar manifests Kubernetes (Deployments, Services, ConfigMaps/Secrets).
3. Adicionar probes (readiness/liveness).
4. Configurar estratégia de rollout:
    - Blue-Green **ou** Canary (documentar escolha e mostrar evidência).

**CI/CD**
5. Pipeline automatizado (ex.: GitHub Actions):
    - build
    - testes
    - push de imagens
    - deploy (ou gerar artefactos)

**Governance**
6. Definir contratos de API:
    - OpenAPI specs por serviço
7. Contract testing (ex.: Pact) para pelo menos um consumer/provider relevante.
8. Definir SLAs mínimos:
    - latência alvo
    - disponibilidade alvo
    - erro máximo tolerado
9. Definir plano de depreciação para uma alteração hipotética de API (ex.: v1 → v2).

**Service Catalog (mínimo viável)**
10. Registar serviços e docs num catálogo (ex.: Backstage ou alternativa equivalente).

### 3.6.2 Critérios de aceitação

- Deploy no Kubernetes funcional, com probes ativas.
- Existe evidência de rollout sem downtime (blue-green/canary).
- Pipeline CI/CD executa automaticamente e produz artefactos.
- OpenAPI disponível e usada como “source of truth”.
- Existe pelo menos um teste de contrato (Pact) demonstrável.
- SLAs e plano de depreciação documentados.
- Service catalog prova a governança (mesmo que seja uma integração mínima).

---

## 4. Deliverables 

Conforme o enunciado, a entrega final inclui:

- **Codebase atualizada** (extensão da P2) com:
    - observabilidade
    - resiliência
    - segurança
    - Saga + event sourcing
- **Deployment scripts**
    - manifests Kubernetes
    - Dockerfiles
    - pipelines CI/CD
- **Documentação**
    - diagramas (arquitetura + security flows + saga orchestration)
    - exemplos de configuração
    - SLAs, contratos e plano de depreciação
    - explicação de Saga + event sourcing aplicada a um caso real
- **Demonstração**
    - dashboards de observabilidade
    - resiliência sob falhas simuladas
    - autenticação segura
    - execução da Saga com event sourcing

---

