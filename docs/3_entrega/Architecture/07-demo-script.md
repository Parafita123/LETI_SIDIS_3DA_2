# 07 – Demo Script (Passo-a-passo)

## 0. Objetivo

Este documento é um guião passo-a-passo para a demonstração ao professor, cobrindo os requisitos principais da P3: observabilidade, resiliência, segurança e deployment/CI-CD. O foco é mostrar evidência prática (dashboards, traces, logs, falhas simuladas e respostas do sistema).

---

## 1. Preparação 
1) Confirmar pré-requisitos locais:
- Docker e Docker Compose instalados
- Portas livres para: gateway, serviços, RabbitMQ, Prometheus, Grafana, Jaeger e UI de logs (se existir)
- Repositório com `docker-compose.yml` e stacks de observability disponíveis

2) Ter à mão:
- Coleção Postman (ou curl commands preparados)
- Credenciais de utilizador (PATIENT / DOCTOR / ADMIN) para demo
- Links/abas abertas:
    - Grafana dashboards
    - Prometheus targets
    - Jaeger UI
    - UI de logs (Kibana/Grafana Explore)

---

## 2. Subir a stack

### 2.1 Arranque

1) Subir toda a stack:
- `docker compose up -d` (ou comando equivalente usado pelo grupo)

2) Validar containers:
- `docker ps`
- Confirmar que estão UP:
    - api-gateway
    - identity-service
    - patient-service (≥ 2 instâncias se aplicável)
    - physician-service (≥ 2 instâncias se aplicável)
    - scheduling-service (≥ 2 instâncias se aplicável)
    - clinical-records-service (≥ 2 instâncias se aplicável)
    - rabbitmq
    - prometheus
    - grafana
    - jaeger
    - logging stack (se aplicável)

3) Validar health checks (se existirem endpoints):
- `GET /actuator/health` ou equivalente em cada serviço

Critério de sucesso:
- Todos os componentes arrancam sem crash e estão acessíveis.

---

## 3. Observability: Logs + Metrics + Traces

## 3.1 Mostrar dashboards de métricas

1) Abrir Prometheus:
- Confirmar targets UP (Status → Targets)

2) Abrir Grafana:
- Abrir dashboard “System Overview”
- Mostrar:
    - requests totais
    - latência
    - erros 4xx/5xx
    - instâncias ativas

3) Gerar tráfego simples (ex.: 5 requests):
- `GET /physicians`
- `GET /patients`
- Confirmar em Grafana que as métricas aumentam.

Critério de sucesso:
- Métricas atualizam em tempo real e há visibilidade por serviço.

---

## 3.2 Mostrar logs centralizados

1) Abrir UI de logs (Kibana ou Grafana Explore)
2) Fazer um request via gateway (ex.: `GET /consultas`)
3) Filtrar logs por:
- service = api-gateway
- correlationId ou traceId (se disponível)
4) Mostrar a sequência:
- gateway recebe request
- serviço executa lógica
- logs de validação/autorização (sem dados sensíveis)
- (se houver) logs de publish/consume AMQP

Critério de sucesso:
- É possível correlacionar logs e seguir o fluxo.

---

## 3.3 Mostrar tracing distribuído (Jaeger)

Objetivo: mostrar um trace completo de um comando crítico.

1) Abrir Jaeger UI
2) Executar um comando forte para demo:
- `POST /consultas` (criar consulta) via gateway

3) No Jaeger:
- Selecionar serviço: api-gateway
- Encontrar o trace mais recente
- Abrir e explicar spans:
    - gateway span (root)
    - scheduling-service span (handler)
    - DB spans (write/read)
    - span de publish AMQP (se existir)
    - span do consumer (se houver atualização de read model)

4) Mostrar como o trace identifica latências e gargalos.

Critério de sucesso:
- Fluxo aparece como um único trace distribuído (REST + AMQP + DB).

---

## 4. Resiliência: simular falha e mostrar comportamento

## 4.1 Cenário: falha do physician-service

Objetivo: demonstrar Timeout/Retry/Circuit Breaker e degradação graciosa.

1) Antes da falha:
- Executar operação que depende de physician-service (ex.: criar consulta que valida physician)
- Confirmar sucesso (status 201 ou equivalente)
- Mostrar métricas baseline no Grafana

2) Induzir falha:
- Desligar uma instância do physician-service:
    - `docker stop <container>` (ou reduzir replicas)

3) Executar o mesmo request novamente:
- `POST /consultas` ou endpoint que force validação physician

4) Mostrar efeitos:
- Response controlada (erro previsível ou fallback)
- Logs com timeout/retry e eventual abertura de circuit breaker
- Métricas a refletir:
    - aumento de erros/timeouts
    - métricas de circuit breaker (se expostas)
- Tracing a mostrar o ponto exato da falha

5) Recuperação:
- Ligar novamente o serviço:
    - `docker start <container>`
- Repetir request e mostrar que o sistema recupera (circuit half-open → closed)

Critério de sucesso:
- O sistema não cai todo.
- Falha é isolada e observável.
- Há recuperação automática.

---

## 4.2 Cenário alternativo (mensageria)

Objetivo: mostrar resiliência em consumers RabbitMQ.

1) Forçar erro num consumer (ex.: payload inválido ou desligar consumer)
2) Publicar evento/ação que gere mensagem
3) Mostrar:
- retries limitados
- encaminhamento para DLQ (se configurado)
- sistema continua a processar outras mensagens

Critério de sucesso:
- Uma mensagem falhada não bloqueia o processamento.

---

## 5. Segurança: 401/403 e permissões

## 5.1 401 – request sem token

1) Fazer request sem header Authorization:
- `GET /consultas`
2) Mostrar resposta:
- `401 Unauthorized`

Critério de sucesso:
- Sem token não há acesso.

---

## 5.2 403 – token válido mas sem permissões

1) Fazer login como PATIENT (token válido)
2) Tentar endpoint restrito (ex.: criar registo clínico):
- `POST /records`
3) Mostrar resposta:
- `403 Forbidden`

Critério de sucesso:
- Token válido não basta; precisa de role/scope.

---

## 5.3 Permissões corretas (sucesso)

1) Fazer login como DOCTOR (token válido com scopes)
2) Executar operação permitida:
- `POST /records` para consulta válida
3) Mostrar sucesso (201)

Critério de sucesso:
- Regras de autorização funcionam e são demonstráveis.

---

## 6. Deployment e CI/CD

## 6.1 Mostrar Kubernetes manifests (se aplicável)

1) Abrir pasta `k8s/` (ou equivalente)
2) Mostrar:
- Deployments (replicas, probes, rollout strategy)
- Services (ClusterIP)
- Ingress (rota do gateway)
- ConfigMaps e Secrets
- HPA (se existir)

Critério de sucesso:
- Existe infraestrutura declarativa e alinhada com práticas de produção.

---

## 6.2 Mostrar pipeline CI/CD (prints ou execução)

1) Abrir GitHub Actions / CI tool
2) Mostrar um run:
- build
- test
- docker build + push
- deploy (ou geração de artefactos)

3) Se não houver deploy automático:
- mostrar que o pipeline produz imagens taggeadas e manifests prontos

Critério de sucesso:
- Existe automação de build/test e evidência clara.

---

## 7. Encerramento  

Mensagem final (30–45 segundos):

- Observability: logs centralizados, métricas em Grafana, tracing em Jaeger (fluxo POST /consultas).
- Resiliência: comportamento sob falha (timeout/retry/circuit breaker) com recuperação.
- Segurança: 401/403 + enforcement de roles/scopes.
- Deployment/CI-CD: Docker + manifests + pipeline automatizado.


---
