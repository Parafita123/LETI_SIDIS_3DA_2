# 03 – Observability

## 0. Objetivo

Este documento descreve a integração de observabilidade no sistema SIDIS/HAP, conforme os requisitos da Parte 3 (P3). O objetivo é garantir visibilidade completa sobre o comportamento do sistema distribuído, permitindo monitorização operacional, diagnóstico de falhas em ambientes multi-instância, análise de fluxos distribuídos (REST + AMQP) e suporte à demonstração e defesa oral do projeto.

A observabilidade foi implementada assente em logging centralizado, métricas e tracing distribuído.

---

## 1. Logging Centralizado

### 1.1 Objetivo

Garantir que todos os serviços produzem logs de forma consistente, enviam logs para um ponto central, permitem correlação de pedidos ponta-a-ponta e suportam análise pós-falha e auditoria mínima.

### 1.2 Arquitetura de logging

A arquitetura de logging segue o modelo:

Microserviços / API Gateway  
↓  
Fluent Bit / Fluentd  
↓  
Storage central (ex.: Elasticsearch / OpenSearch / Loki)  
↓  
UI de visualização (Kibana / Grafana)

O runtime (Docker/Kubernetes) recolhe automaticamente os logs stdout/stderr de cada instância.

### 1.3 Convenções de logging

Os logs seguem um formato estruturado e incluem, sempre que aplicável:

- timestamp
- level (INFO, WARN, ERROR)
- service (nome do microserviço)
- instanceId / container / pod
- correlationId (propagado a partir do API Gateway)
- traceId / spanId (quando tracing está ativo)
- message
- opcionalmente eventId, messageId, sagaId

Dados sensíveis (credenciais, tokens completos, informação clínica detalhada) não são registados em logs.

### 1.4 Instrumentação por serviço (logging)

**API Gateway**
- Logs de entrada e saída de cada request HTTP
- Geração e propagação de X-Correlation-Id
- Logs de decisões de routing e autorização

**Identity Service**
- Emissão e validação de tokens
- Falhas de autenticação e autorização

**Patient Service**
- Logs de endpoints de leitura e escrita
- Logs de validações de negócio
- Logs de consumo/publicação de eventos RabbitMQ

**Physician Service**
- Logs de CRUD e pesquisa
- Logs de validações e erros

**Scheduling Service**
- Logs de criação e alteração de estado de consultas
- Logs de publicação de eventos AMQP
- Logs de consumers e atualizações de read models

**Clinical Records Service**
- Logs de criação, edição e consulta de registos clínicos
- Logs de validações síncronas com outros serviços
- Logs de falhas, retries e timeouts

### 1.5 Como validar (Logging)

1. Executar um request (ex.: POST /consultas) via API Gateway.
2. Aceder à UI de logs (Kibana/Grafana).
3. Filtrar por correlationId.
4. Verificar que aparecem logs do gateway, do scheduling service e de eventuais consumers AMQP.
5. Simular uma falha (ex.: desligar uma instância) e observar logs de erro e fallback.

Critério de aceitação:
- É possível seguir um pedido completo através dos logs usando correlationId.
- Logs de AMQP incluem identificação da mensagem.
- Logs são centralizados e pesquisáveis.

---

## 2. Metrics (Prometheus + Grafana)

### 2.1 Objetivo

Disponibilizar métricas quantitativas que permitam avaliar desempenho, identificar gargalos, monitorizar estado de serviços e filas e apoiar a demonstração.

### 2.2 Métricas recolhidas

**HTTP (por serviço)**
- Número total de requests
- Latência (p50, p95, p99)
- Taxa de erros (4xx, 5xx)

**Runtime / JVM**
- CPU
- Memória
- Threads
- Garbage Collection

**Base de dados**
- Conexões ativas
- Tempo médio de query (quando disponível)

**RabbitMQ**
- Mensagens publicadas
- Mensagens consumidas
- Profundidade das filas
- Taxa de consumo
- Mensagens em DLQ (se aplicável)

### 2.3 Dashboards (Grafana)

Dashboards configurados incluem:

1. Visão geral do sistema
    - Requests totais
    - Erros totais
    - Latência média
    - Instâncias ativas

2. Por microserviço
    - Latência por endpoint
    - Erros por endpoint

3. Mensageria (RabbitMQ)
    - Queue depth
    - Publish rate
    - Consumer rate

### 2.4 Como validar (Metrics)

1. Aceder ao Prometheus e confirmar targets UP.
2. Abrir Grafana e visualizar dashboards.
3. Executar requests repetidos (ex.: múltiplos POST /consultas).
4. Confirmar incremento de contadores e atualização de latência.
5. Induzir falha e observar aumento de erros e latência.

Critério de aceitação:
- Existem dashboards funcionais por serviço.
- Métricas refletem alterações de carga e falhas.
- RabbitMQ apresenta métricas visíveis.

---

## 3. Tracing Distribuído (Jaeger / OpenTelemetry)

### 3.1 Objetivo

Permitir a análise de fluxos distribuídos ponta-a-ponta, incluindo chamadas REST, publicação e consumo AMQP e operações de base de dados.

### 3.2 Estratégia de tracing

- Uso de OpenTelemetry para instrumentação
- Propagação de contexto via HTTP headers e metadata AMQP
- Visualização em Jaeger (ou equivalente)

### 3.3 Instrumentação por serviço (Tracing)

**API Gateway**
- Criação do span raiz por request
- Propagação de contexto downstream

**Scheduling Service**
- Span do endpoint POST /consultas
- Spans de chamadas HTTP internas
- Span de publish de evento AMQP
- Spans de acesso à base de dados

**Clinical Records, Patient e Physician Services**
- Spans por endpoint
- Spans em consumers RabbitMQ
- Spans de operações de leitura/escrita na DB

**RabbitMQ**
- Producer span (publish)
- Consumer span (processamento)

### 3.4 Como validar (Tracing)

1. Abrir Jaeger UI.
2. Executar POST /consultas via gateway.
3. Pesquisar traces pelo serviço api-gateway.
4. Verificar que o trace contém spans do gateway, serviços envolvidos, AMQP e DB.
5. Induzir falha e confirmar que o erro aparece marcado no trace.

Critério de aceitação:
- Um fluxo real aparece como um único trace distribuído.
- O trace inclui REST e AMQP.
- O ponto de falha é claramente identificável.

---

## 4. Health Checks (Liveness & Readiness)

### 4.1 Objetivo

Permitir que o orquestrador reinicie instâncias bloqueadas e retire instâncias não prontas do tráfego.

### 4.2 Estratégia

**Liveness**
- Verifica que a aplicação está funcional.

**Readiness**
- Verifica dependências críticas como base de dados e RabbitMQ.

### 4.3 Como validar (Health Checks)

1. Aceder aos endpoints de health por serviço.
2. Simular falha de DB ou broker.
3. Confirmar que readiness falha e a instância deixa de receber tráfego.
4. Simular deadlock e verificar reinício automático.

Critério de aceitação:
- O sistema distingue instâncias vivas vs prontas.
- Falhas não causam indisponibilidade global.


---
