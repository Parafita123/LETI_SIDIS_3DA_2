# 02 – Gap Analysis 

## 0. Objetivo deste documento

Este documento descreve, de forma sintética, o **estado atual** do sistema (resultado da Parte 1) e compara-o com os **requisitos da Parte 2**.

O objetivo é:

- Tornar explícito **o que já está feito**.
- Identificar **o que falta implementar / refatorar** para cumprir a Parte 2.
- Servir de guia para as próximas iterações de arquitetura, implementação e testes.

---

## 1. Visão geral – Estado atual vs. objetivos da Parte 2

### 1.1 Estado atual (Parte 1)

De forma resumida, o sistema atualmente tem:

- Arquitetura de **microserviços** alinhada com o domínio HAP:
    - `patient-service`, `physician-service`, `scheduling-service`, `clinical-records-service`, `identity-service`, `api-gateway`.
- **Database-per-service**: cada microserviço tem a sua própria base de dados, sem acessos diretos entre DBs.
- Comunicação **HTTP síncrona** entre serviços (via REST), com validações cruzadas baseadas em IDs.
- Autenticação/autorização via **Identity Service** com **JWT** e scopes/roles.
- Algumas **mecanismos de resiliência** já presentes (timeouts, retries e circuit-breaker configurados no clinical-records).
- Conjunto inicial de **testes funcionais** (por exemplo, via Postman/HTTP) focados em cenários de um único serviço ou fluxos síncronos.

### 1.2 Objetivos principais da Parte 2 (alto nível)

A Parte 2 adiciona requisitos avançados de sistemas distribuídos, entre os quais:

- Suporte a **múltiplas instâncias por microserviço** (≥ 2 instâncias de cada).
- Introdução de **padrão CQRS** (separação clara entre comandos e queries) a nível de API e/ou internamente.
- Utilização de **AMQP / message broker** para comunicação assíncrona entre instâncias/serviços (eventos de domínio, propagação de estado, eventual consistency).
- **Armazenamento por instância** (database-per-microservice-instance ou, no mínimo, read models isolados por instância).
- **Testes de integração distribuídos**, cobrindo cenários com várias instâncias, falhas parciais, consistência eventual, etc.

---

## 2. Tabela de gaps – Requisitos por tema

### 2.1 Arquitetura e deployment

| Requisito / Tema                                      | Estado atual (Parte 1)                                                                                                 | GAP / Trabalho necessário (Parte 2)                                                                                                       |
|-------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| Microserviços independentes                           | **Feito parcialmente.** Serviços separados por domínio (patients, physicians, scheduling, records, identity, gateway). | Manter a separação, mas garantir que suportam múltiplas instâncias e comunicação assíncrona entre elas.                                  |
| Base de dados por serviço (database-per-service)      | **Feito.** Cada serviço tem a sua DB e não acede diretamente à DB de outros.                                           | Necessário evoluir para um modelo alinhado com **database-per-instance** (especialmente para read models / CQRS).                       |
| Múltiplas instâncias por microserviço                 | **Não.** Apenas 1 instância de cada serviço é considerada no deployment atual.                                         | Definir e implementar deployment com ≥ 2 instâncias de cada serviço (por ex. via docker-compose/swarm/k8s) e atualizar documentação.    |
| Balanceamento de carga / API Gateway                  | **Parcial.** API Gateway expõe endpoint único, mas sem foco explícito em load balancing por instância.                 | Configurar o gateway (ou outro componente) para encaminhar pedidos para múltiplas instâncias de cada serviço.                           |
| Configuração específica por instância                 | **Não relevante ainda.** Config igual para cada serviço.                                                               | Definir como diferenciar instâncias (por ex. identificador lógico, porta, DB própria) e documentar o modelo.                            |

---

### 2.2 Comunicação entre serviços

| Requisito / Tema                         | Estado atual (Parte 1)                                                                                           | GAP / Trabalho necessário (Parte 2)                                                                                      |
|------------------------------------------|------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| Comunicação síncrona HTTP/REST           | **Feito.** Serviços comunicam por HTTP para validações cruzadas (records > physicians/patients/scheduling, etc). | Manter para endpoints externos e alguns fluxos internos que façam sentido.                                              |
| Comunicação assíncrona via AMQP/Broker   | **Não.** Não existe atualmente broker/mensageria (RabbitMQ, etc.).                                               | Introduzir um message broker (AMQP), definir **eventos de domínio** e integrar serviços com publishers/subscribers.     |
| Eventos de domínio (event-driven)        | **Não.** Fluxos baseiam-se em pedidos/respostas síncronos.                                                       | Modelar eventos (ex.: `ConsultationScheduled`, `ConsultationCancelled`, `RecordCreated`, `PatientRegistered`, etc.).    |
| Consistência eventual entre serviços     | **Não.** Assumido comportamento quase totalmente síncrono.                                                       | Definir onde a consistência pode ser eventual, usando eventos + read models, e documentar implicações.                  |
| Fan-out para peers / replicação lógica   | **Não / Planeado.** Conceptualmente referido, mas não implementado.                                              | Implementar lógica de fan-out/leitura entre instâncias (quando necessário) ou justificar outra abordagem via CQRS.     |

---

### 2.3 Dados e armazenamento

| Requisito / Tema                               | Estado atual (Parte 1)                                                                                         | GAP / Trabalho necessário (Parte 2)                                                                                                                           |
|-----------------------------------------------|------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Database-per-service                           | **Feito.** Cada microserviço tem DB própria e isolada.                                                          | Manter – é a base.                                                                                                                                            |
| Database-per-microservice-instance             | **Não.** DB desenhada por serviço, não por instância.                                                           | Definir estratégia: por exemplo, **write DB partilhada por serviço + read DB por instância (CQRS)** e implementar para pelo menos um fluxo completo.        |
| Read models dedicados para queries             | **Não ou muito limitado.** Leitura e escrita usam o mesmo modelo/DB.                                            | Introduzir read models específicos (tabelas/estruturas otimistas para queries), idealmente **um por instância** para cumprir os requisitos de escalabilidade. |
| Gestão de migrações / esquema com múltiplas DB | **Básico.** Migrações/DDL pensados por serviço.                                                                  | Rever estratégia de migrations para incluir read DBs por instância e documentar como são criados/atualizados.                                               |
| Estratégias de cache                           | **Pouco ou nenhum.** Cache ainda não é um componente central.                                                   | Avaliar cache local de read models e/ou cache de respostas de peers, documentando TTLs, invalidação e impacto em consistência eventual.                     |

---

### 2.4 Padrões de arquitetura (CQRS, resiliência, etc.)

| Requisito / Tema                 | Estado atual (Parte 1)                                                                                              | GAP / Trabalho necessário (Parte 2)                                                                                                      |
|----------------------------------|-----------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| CQRS (Command Query Responsibility Segregation) | **Não.** Endpoints atuais misturam comandos e queries no mesmo modelo e DB.                                         | Definir comandos e queries por serviço, introduzir handlers distintos e, idealmente, read models próprios para queries.                |
| Separação clara de comandos/queries a nível de API | **Parcial.** Nomes de endpoints indicam função, mas não existe uma separação arquitetural formal.                    | Documentar e implementar a separação (p.ex. camadas/handlers diferentes, módulos ou mesmo serviços lógicos de comando vs. query).      |
| Resiliência (timeouts, retries, circuit breaker) | **Parcial.** Clinical Records já tem timeouts, retry e circuit breaker configurados para chamadas HTTP remotas.      | Estender/ajustar para o novo cenário com broker + múltiplas instâncias e documentar os parâmetros por tipo de chamada (HTTP vs AMQP). |
| Tolerância a falhas com múltiplas instâncias     | **Não.** Foco atual é em falhas de chamadas HTTP singulares, não em falha de instância/partição de dados.           | Definir comportamento esperado quando uma instância/DB está indisponível e como as restantes instâncias lidam com isso (fallback, etc). |

---

### 2.5 Testes, observabilidade e documentação

| Requisito / Tema                           | Estado atual (Parte 1)                                                                                         | GAP / Trabalho necessário (Parte 2)                                                                                                    |
|--------------------------------------------|------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| Testes unitários por serviço               | **Parcial/Feito.** Existem testes unitários ou de serviço pontuais.                                             | Garantir boa cobertura das regras de domínio mais críticas.                                                                          |
| Testes de integração simples (single instance) | **Parcial.** Existem collections / cenários ponta-a-ponta mas assumindo 1 instância por serviço.                 | Criar **testes de integração distribuídos** que verifiquem: CQRS, eventos, propagação entre instâncias, consistência eventual, etc.  |
| Testes focados em falhas e recuperação     | **Poucos ou nenhuns.** Falhas tratadas nos fluxos mais básicos.                                                 | Criar cenários de falha: queda de uma instância, latências no broker, timeouts em peers, conflitos de escrita, etc.                  |
| Observabilidade (logs, métricas, tracing)  | **Básico.** Logging standard, sem tracing distribuído.                                                           | Documentar mínimos de observabilidade: correlação de requests entre serviços, métricas úteis (latência, taxa de erro, etc.).        |
| Documentação de arquitetura (C4+1)         | **Em evolução.** `01-current-architecture` descreve Parte 1.                                                     | Complementar com documentos para a nova arquitetura (CQRS, messaging, deployment multi-instância, etc.).                             |

---

## 3. Síntese dos principais gaps

Os principais **gaps estruturais** identificados são:

1. **Ausência de CQRS formal**
    - Atualmente, leitura e escrita partilham o mesmo modelo e DB.
    - A Parte 2 exige uma separação clara, idealmente com read models otimizados e independentes (por instância).

2. **Falta de comunicação assíncrona via AMQP / broker**
    - Todo o sistema assenta em HTTP síncrono.
    - É necessário introduzir eventos de domínio e um broker para:
        - Propagar mudanças entre instâncias e serviços.
        - Permitir consistência eventual e escalabilidade de leitura.

3. **Deployment ainda single-instance**
    - Só existe uma instância de cada microserviço.
    - É obrigatório suportar (e testar) múltiplas instâncias, o que implica:
        - Gestão de state por instância (DBs/read models).
        - Decisões sobre load balancing, fan-out e fallback.

4. **Modelo de dados ainda apenas per-service, não per-instance**
    - Embora corretamente isolado por serviço, ainda não reflete a visão de “cada instância tem o seu read model local”.
    - É necessário rever a arquitetura de persistência para acomodar **database-per-instance** (pelo menos para o lado de leitura).

5. **Testes de integração ainda centrados em cenários simples / single-instance**
    - Falta uma bateria de testes para:
        - Verificar que eventos são publicados/consumidos.
        - Confirmar que as várias instâncias convergem para o mesmo estado visível.
        - Validar o comportamento perante falhas parciais.

---

## 4. Próximos passos

Com base nesta análise, os próximos passos lógicos para a Parte 2 são:

1. **Escolher um caso de uso “piloto”** (por exemplo, “Paciente agenda consulta e vê lista de consultas futuras”) e aplicar **CQRS + AMQP + múltiplas instâncias** de ponta a ponta.
2. **Definir o modelo de comandos, queries e eventos** para esse fluxo, documentando em detalhe:
    - Comandos (`ScheduleConsultation`, `CancelConsultation`, `RecordAppointmentDetails`, etc.).
    - Queries (`ListConsultationsByPatient`, `GetConsultationDetails`, etc.).
    - Eventos (`ConsultationScheduled`, `ConsultationCancelled`, `RecordCreated`, ...).
3. **Desenhar e implementar a estratégia de armazenamento por instância** para o serviço escolhido:
    - Write DB (partilhada por serviço ou não).
    - Read DB local por instância (alimentada via eventos).
4. **Configurar o deployment com ≥ 2 instâncias** das componentes relevantes e atualizar o API Gateway / configs de rede.
5. **Criar testes de integração distribuídos** (Postman, scripts, etc.) que mostrem claramente:
    - Fluxo normal com múltiplas instâncias.
    - Propagação assíncrona de dados.
    - Comportamento em caso de falha parcial.

Este documento serve como base para os próximos artefactos de arquitetura:

- `03-cqrs-overview.md`
- `04-messaging-and-broker.md`
- `05-deployment-multi-instance.md`

Onde cada um destes tópicos será detalhado com diagramas, decisões e exemplos concretos do sistema.
