# C4 Nível 3 - Diagrama de Componentes: Scheduling Service

## 1. Visão Geral
O **Scheduling Service** é o núcleo funcional do sistema HAP. É neste serviço que reside a complexidade da orquestração de agendamentos. Para cumprir os requisitos de consistência em transações distribuídas e auditoria rigorosa, este serviço abandona a persistência de estado tradicional em favor de **Event Sourcing** puro, suportado por uma arquitetura **CQRS**.

## 2. Pontos de Entrada
O serviço aceita comandos de duas fontes distintas, protegidas pelo *Security Filter*:

*   **Schedule Controller (Síncrono/HTTP):** Recebe os pedidos diretos dos utentes (ex: "Marcar Consulta"). A sua função é apenas validar o formato do pedido e passá-lo ao domínio, retornando rapidamente um `202 Accepted` para não bloquear o cliente.
*   **Saga Event Listener (Assíncrono/AMQP):** Este componente é fundamental para o padrão **Saga**. Ele fica à escuta de eventos de resposta dos outros serviços (`PatientValidated`, `PhysicianConfirmed`). É através dele que o processo de agendamento avança assincronamente.

## 3. Arquitetura Interna: O Motor de Eventos (Command Side)
A vertente de escrita é desenhada para garantir atomicidade e consistência sem usar *locks* de base de dados tradicionais:

*   **Saga Aggregator (State Machine):** Este é o componente mais crítico. Como não guardamos o "estado atual" da consulta (ex: `PENDING`), sempre que chega um evento novo, o Aggregator executa a lógica de **Reidratação**:
    1.  Solicita ao *Event Store Repo* todo o histórico de eventos daquela consulta.
    2.  Reconstrói o objeto em memória (ex: "Já tenho a validação do Paciente? Sim. E do Médico? Não.").
    3.  Decide o próximo passo (ex: Esperar ou emitir `AppointmentBooked`).
*   **Scheduling Command Service:** Coordena a lógica de negócio. Se o Aggregator confirmar que a transação pode ser concluída, este serviço gera o novo evento.
*   **Event Publisher:** Publica os eventos resultantes (`Created`, `Booked`, `Cancelled`) no RabbitMQ, alimentando tanto os outros microsserviços como a própria projeção de leitura interna.

## 4. Persistência Poliglota e Segregada (CQRS)
O diagrama demonstra o uso de duas tecnologias de base de dados distintas, otimizadas para os seus propósitos:

*   **Event Store DB (PostgreSQL):** Utilizada via *Event Store Repo* (JDBC). Armazena o log imutável de eventos. O PostgreSQL foi escolhido aqui pela sua robustez e garantias ACID na gravação sequencial de eventos críticos.
*   **Read DB (H2):** Utilizada via *Read Model Repo* (JPA). Armazena as "Views" ou projeções. Como as leituras são frequentes mas menos críticas em termos de durabilidade a longo prazo (podem ser reconstruídas), usa-se H2 para performance.

## 5. Sincronização (Scheduling Projector)
Para que o *Query Service* possa responder a pedidos GET, o **Scheduling Projector** escuta os eventos gerados internamente e atualiza as tabelas no H2.
*   *Exemplo:* Quando ocorre um `AppointmentBooked`, o Projector insere uma linha na tabela de leitura com os dados finais (Data, Médico, Paciente), pronta a ser consultada.

## 6. Resiliência
O componente **Resilience Aspect** (Resilience4j) envolve as interações com as bases de dados.
*   **TimeLimiter:** Se a reconstrução do histórico (Reidratação) demorar demasiado tempo, o processo é abortado para evitar *thread starvation*.
*   **Circuit Breaker:** Protege o sistema caso o PostgreSQL ou o H2 fiquem indisponíveis.