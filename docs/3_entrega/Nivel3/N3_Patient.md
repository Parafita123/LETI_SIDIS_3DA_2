# C4 Nível 3 - Diagrama de Componentes: Patient Service

## 1. Visão Geral
O Diagrama de Componentes do **Patient Service** revela a estrutura interna de um microsserviço desenhado para operar num ambiente distribuído e orientado a eventos. Ao contrário de uma arquitetura em camadas tradicional (Controller-Service-Repository), este serviço adota internamente o padrão **CQRS** (Command Query Responsibility Segregation) para separar fisicamente e logicamente as operações de escrita das operações de leitura.

## 2. Camada de Entrada e Segurança
O serviço possui dois vetores de entrada distintos, protegidos por um **Security Filter** que valida Tokens JWT e certificados mTLS:

1.  **Entrada Síncrona (Patient Controller):** Expõe endpoints REST (HTTP) para clientes externos (ex: Frontend, Admins). Recebe pedidos de registo ou consulta de perfil.
2.  **Entrada Assíncrona (Saga Event Listener):** Um componente crítico para o padrão **Saga**. Este *listener* subscreve filas do RabbitMQ para reagir a eventos de outros serviços (ex: `AppointmentCreatedEvent` vindo do Scheduling Service), desencadeando validações automáticas sem intervenção humana.

## 3. Núcleo de Negócio (CQRS)
A lógica de negócio está dividida em dois fluxos unidirecionais:

### A. Command Side (Escrita e Validação)
Responsável por alterar o estado do sistema.
*   **Patient Command Service:** Implementa as regras de negócio complexas (ex: verificar dívidas, validar seguros). Em vez de atualizar o estado atual, gera novos eventos.
*   **Event Publisher:** Após o processamento do comando, publica eventos de domínio (ex: `PatientRegistered`, `PatientValidated`) no Message Broker. Isto permite que outros serviços saibam o que aconteceu.
*   **Event Store Repo:** Persiste os eventos gerados na base de dados (`Event Store`).

### B. Query Side (Leitura)
Responsável por fornecer dados rapidamente.
*   **Patient Query Service:** Otimizado para pesquisas. Aplica filtros de segurança (GDPR) para garantir que dados sensíveis só são retornados a quem tem permissão explícita.
*   **View Repo:** Acede a uma tabela desnormalizada (`Read DB`) que contém o estado atual do paciente, pronta a ser lida sem *joins* complexos.

### C. Sincronização (The Projector)
*   **Patient Projector:** É o componente que liga os dois mundos. Ele escuta os eventos gerados pelo lado da escrita e atualiza a base de dados de leitura (`Upsert View`). Garante a **Consistência Eventual** do sistema.

## 4. Persistência e Infraestrutura
O serviço utiliza uma estratégia de persistência segregada (mesmo usando H2 como motor):

*   **Event Store (H2):** Tabela *Append-Only* que guarda o histórico imutável de tudo o que aconteceu ao paciente.
*   **Read DB (H2):** Tabela otimizada ("Projeção") que reflete o estado atual, atualizada pelo Projector.
*   **Resilience Aspect (Resilience4j):** Uma camada transversal que protege os repositórios com padrões como **Circuit Breaker**. Se a base de dados estiver lenta ou indisponível, o componente falha rapidamente (*fail-fast*) para não bloquear threads do servidor.

## 5. Participação na Saga (Agendamento)
O diagrama ilustra claramente como o serviço participa na Saga de Agendamento:
1.  O **Saga Event Listener** recebe `AppointmentCreated`.
2.  O **Command Service** valida se o paciente pode ter consultas.
3.  O **Event Publisher** envia `PatientValidated` para o RabbitMQ, permitindo que o *Scheduling Service* prossiga.