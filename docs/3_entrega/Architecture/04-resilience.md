# 04 – Resilience and Fault Tolerance

## 0. Objetivo

Este documento descreve a implementação de **resiliência e tolerância a falhas** no sistema SIDIS/HAP, conforme exigido na Parte 3 (P3).

O objetivo é garantir que o sistema distribuído:
- se comporta de forma previsível perante falhas parciais,
- evita falhas em cascata,
- degrada graciosamente,
- mantém consistência através de mecanismos de compensação (ex.: Sagas).

Foram aplicados os seguintes padrões de resiliência:
- Timeout
- Retry
- Circuit Breaker
- Bulkhead
- Fallback (quando aplicável)

---

## 1. Visão geral da estratégia de resiliência

Num sistema distribuído com:
- múltiplas instâncias,
- comunicação REST e AMQP,
- CQRS, Sagas e consistência eventual,

as falhas são consideradas **normais e esperadas**, não exceções.

A estratégia adotada baseia-se em:
- deteção rápida de falhas,
- isolamento de componentes problemáticos,
- recuperação automática sempre que possível,
- visibilidade total via observability (logs, métricas, tracing).

---

## 2. Padrões aplicados

## 2.1 Timeout

### Objetivo

Evitar que uma chamada bloqueie indefinidamente e cause:
- saturação de threads,
- degradação global,
- falhas em cascata.

### Onde é aplicado

- Chamadas HTTP inter-serviços:
    - Scheduling → Physician
    - Scheduling → Patient
    - Clinical Records → Scheduling / Physician / Patient
- Chamadas ao broker AMQP (consumo/publicação)
- Acesso à base de dados (via pool/configuração)

### Configuração típica

- Timeout curto e explícito por tipo de chamada:
    - HTTP: centenas de ms a poucos segundos
    - AMQP: timeout no processamento do consumer
- Timeouts definidos no client (não apenas no servidor)

### Critério de aceitação

- Uma chamada lenta falha rapidamente.
- Threads não ficam bloqueadas indefinidamente.
- O erro é visível em logs, métricas e tracing.

---

## 2.2 Retry

### Objetivo

Recuperar automaticamente de falhas **transitórias**, como:
- latência temporária,
- falha momentânea de uma instância,
- pequenos problemas de rede.

### Onde é aplicado

- Chamadas HTTP idempotentes (GET, validações)
- Consumo de mensagens AMQP
- Operações de leitura de read models

### Estratégia adotada

- Número limitado de retries
- Backoff progressivo (exponencial ou fixo)
- Retry apenas para erros elegíveis (timeouts, 5xx)

### Onde **não** é aplicado

- Operações não idempotentes sem proteção adicional
- Commands que possam causar duplicação sem idempotência

### Critério de aceitação

- Falhas transitórias são recuperadas automaticamente.
- Não existe retry infinito.
- Retries são visíveis em métricas e logs.

---

## 2.3 Circuit Breaker

### Objetivo

Evitar chamadas repetidas a um componente degradado e prevenir falhas em cascata.

### Estados do Circuit Breaker

- **CLOSED** – funcionamento normal
- **OPEN** – chamadas bloqueadas após excesso de falhas
- **HALF-OPEN** – chamadas de teste para verificar recuperação

### Onde é aplicado

- Chamadas HTTP críticas entre serviços:
    - Scheduling → Physician
    - Clinical Records → Scheduling
- Integração com serviços usados em Sagas
- Pontos onde uma falha pode impactar múltiplos fluxos

### Integração com observability

- Logs de transição de estado
- Métricas de circuit breaker (open/close)
- Spans marcados como erro em tracing

### Critério de aceitação

- Após falhas repetidas, o circuito abre.
- Chamadas deixam de ser executadas contra o serviço degradado.
- Após recuperação, o circuito fecha automaticamente.

---

## 2.4 Bulkhead

### Objetivo

Isolar recursos para impedir que uma parte do sistema esgote recursos globais.

### Onde é aplicado

- Isolamento de pools de threads para:
    - chamadas HTTP externas
    - consumers AMQP
- Separação de recursos entre:
    - command side
    - query side

### Estratégia adotada

- Limitação explícita de concorrência
- Filas ou pools separados por tipo de operação
- Proteção contra overload em cenários de falha

### Critério de aceitação

- Saturação de um componente não afeta os restantes.
- O sistema continua parcialmente funcional sob carga/falha.

---

## 2.5 Fallback

### Objetivo

Definir comportamento alternativo quando:
- timeout/retry/circuit breaker falham,
- o serviço remoto está indisponível.

### Onde é aplicado

- Queries (ex.: devolver lista vazia, cache, erro controlado)
- Leitura de read models locais
- Sagas (ativação de compensações)

### Nota importante

Fallback **não significa esconder erros**, mas sim:
- devolver resposta controlada,
- manter consistência,
- permitir recuperação posterior.

### Critério de aceitação

- O sistema não falha abruptamente.
- O fallback é consistente com as regras de negócio.
- O erro é registado e observável.

---

## 3. Resiliência em Sagas e Mensageria

### 3.1 Integração com Sagas

Nos fluxos baseados em Saga:
- cada step é protegido por timeout e circuit breaker,
- falhas disparam **ações de compensação**,
- o estado da Saga é persistido e rastreável.

### 3.2 AMQP / RabbitMQ

Resiliência aplicada a:
- consumidores idempotentes,
- retries com backoff,
- Dead Letter Queues (DLQ) para mensagens problemáticas.

### Critério de aceitação

- Uma mensagem com erro não bloqueia a fila.
- Mensagens falhadas são encaminhadas para DLQ.
- O sistema continua a processar outras mensagens.

---

## 4. Cenários de falha para demonstração

### 4.1 Falha de serviço remoto

**Cenário**
- Desligar uma instância do `physician-service`.

**Comportamento esperado**
- Scheduling tenta validar médico.
- Ocorrem timeouts e retries.
- Circuit breaker abre.
- Logs e métricas refletem o estado OPEN.
- O sistema devolve erro controlado ou ativa fallback.

---

### 4.2 Falha durante uma Saga

**Cenário**
- Criar consulta.
- Falhar validação num step intermédio.

**Comportamento esperado**
- Saga entra em estado de falha.
- Executa compensações.
- Estado final consistente.
- Trace mostra a Saga completa com erro marcado.

---

### 4.3 Falha no consumo de mensagens

**Cenário**
- Forçar erro num consumer RabbitMQ.

**Comportamento esperado**
- Retry limitado.
- Mensagem enviada para DLQ.
- Outras mensagens continuam a ser processadas.
- Métricas e logs refletem a situação.

---

## 5. Configuração

A resiliência é configurada através de bibliotecas como **Resilience4j**, com parâmetros ajustados por tipo de chamada.

Exemplos de parâmetros configuráveis:
- timeoutDuration
- maxAttempts
- waitDuration (backoff)
- failureRateThreshold
- slidingWindowSize
- permittedNumberOfCallsInHalfOpenState

Os valores exatos são definidos por serviço, consoante criticidade e perfil de carga.

---

## 6. Critérios globais de aceitação

- O sistema não falha em cascata.
- Falhas são detetadas rapidamente.
- O impacto é isolado.
- A recuperação é automática quando possível.
- Todos os comportamentos são observáveis (logs, métricas, tracing).


---
