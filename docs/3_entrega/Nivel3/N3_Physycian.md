# C4 Nível 3 - Diagrama de Componentes: Physician Service

## 1. Visão Geral
O **Physician Service** é o microsserviço responsável pela gestão do corpo clínico e, crucialmente, pela gestão de **Escalas e Disponibilidade**. Neste nível de detalhe, observa-se que o serviço foi refatorizado para suportar **CQRS** e **Event Sourcing**, abandonando a arquitetura monolítica de camadas tradicional em favor de uma estrutura orientada a eventos.

## 2. Pontos de Entrada e Segurança
O serviço expõe interfaces para interação humana e sistémica, protegidas por um **Security Filter** robusto (JWT e mTLS):

*   **Physician Controller (Síncrono/HTTP):** Permite a gestão administrativa (ex: criar médico, definir horário de trabalho) e consultas de leitura (ex: "Quais os meus horários hoje?").
*   **Saga Event Listener (Assíncrono/AMQP):** Este componente liga o serviço ao barramento de eventos (RabbitMQ). A sua função principal é escutar o evento `AppointmentCreatedEvent`. Quando este evento chega, o *listener* aciona internamente a lógica de validação de disponibilidade sem bloquear o serviço de agendamento.

## 3. Arquitetura Interna (CQRS)
A lógica de negócio está segregada para otimizar a consistência na escrita e a performance na leitura:

### A. Command Side (Gestão de Estado e Validação)
*   **Physician Command Service:** Contém a lógica "dura" do domínio. Quando recebe um pedido de agendamento (via Saga), calcula se há sobreposição de horários.
*   **Event Publisher:** Se a validação for bem-sucedida, não altera dados diretamente na base de leitura. Em vez disso, publica o evento `PhysicianAvailabilityConfirmed`. Se falhar, publica uma rejeição.
*   **Event Store Repository:** Persiste as alterações como uma sequência de eventos imutáveis na base de dados H2 (`INSERT` apenas), garantindo um histórico auditável das alterações de escalas.

### B. Query Side (Leitura)
*   **Physician Query Service:** Dedicado a responder a pedidos de informação.
*   **Read Model Repository:** Acede a tabelas desnormalizadas ("Views") na base H2. Estas tabelas contêm a informação pronta a consumir (ex: JSON de perfil de médico), permitindo leituras muito rápidas (`SELECT`) sem necessidade de reconstruir o histórico de eventos a cada pedido.

### C. Sincronização (Projector)
*   **Physician Projector:** Atua como um manipulador de eventos interno. Sempre que um evento é gravado (ex: `ScheduleUpdated`), o Projector captura-o e atualiza a tabela de leitura (*Read Model*). Isto mantém as duas vertentes do CQRS sincronizadas com **Consistência Eventual**.

## 4. Resiliência e Infraestrutura
O diagrama destaca a preocupação com a estabilidade do sistema num ambiente distribuído:

*   **Resilience Aspect (Resilience4j):** Envolve os repositórios de dados. Implementa padrões como **Circuit Breaker**. Se a base de dados H2 ficar bloqueada ou lenta, o sistema falha graciosamente (*Fail Fast*) em vez de esgotar as threads do servidor, impedindo que uma falha de base de dados se propague ao RabbitMQ.
*   **Persistência Híbrida em H2:** O diagrama ilustra o uso eficiente da base H2 para suportar tanto o *Event Store* (Log) como as *Views* (Relacional), demonstrando que o padrão CQRS pode ser implementado com tecnologias de base de dados simples, desde que o design de código seja correto.

## 5. Papel na Saga de Agendamento
Este componente é vital para a transação distribuída:
1.  Recebe `AppointmentCreated` (via Listener).
2.  Verifica matematicamente a colisão de horários (`Command Service`).
3.  Emite `PhysicianAvailabilityConfirmed` ou `Rejected` (via Publisher).
    Este fluxo desacoplado garante que o Agendamento pode escalar independentemente da complexidade da gestão de escalas médicas.