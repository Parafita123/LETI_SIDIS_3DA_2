# Fluxo de Agendamento de Consulta (Saga & Event Sourcing)

Este diagrama representa o processo "Core" do sistema HAP: a criação de uma consulta médica. Para cumprir os requisitos de alta disponibilidade e desacoplamento do Assignment 3, abandonou-se a abordagem monolítica tradicional em favor de uma **Saga Coreografada** suportada por **Event Sourcing**.

## Conceitos Arquiteturais Fundamentais

1.  **Event Sourcing (Append-Only):**
    *   A base de dados de escrita (*Event Store*) nunca sofre atualizações (`UPDATE`). O estado de uma consulta não é uma linha numa tabela, mas sim a soma de todos os eventos associados a um ID (`Created`, `Validated`, `Confirmed`, `Booked`).
    *   Isto garante um histórico auditável perfeito e facilita a gestão de concorrência.

2.  **Saga Coreografada (Paralelismo):**
    *   Não existe um orquestrador central a "mandar" nos serviços. O *Scheduling Service* emite um evento e os outros serviços (*Patient* e *Physician*) reagem a ele autonomamente e em paralelo.
    *   Isto reduz a latência total do processo, pois as validações do paciente e do médico ocorrem ao mesmo tempo.

3.  **O Papel do Agregador (Passo 3):**
    *   O *Scheduling Command API* atua como um "Agregador Stateful". Sempre que recebe um evento de sucesso (`PatientValidated` ou `PhysicianConfirmed`), ele executa a **Reidratação** (lê o histórico de eventos).
    *   A consulta só transita para o estado `Booked` quando o histórico confirma que **ambas** as validações necessárias foram recebidas.

4.  **Observabilidade (Trace-ID):**
    *   Dado que o processo salta entre 4 serviços e um Message Broker, o **Trace-ID** (`tr-2025`) é vital. Ele permite visualizar no Jaeger o "caminho crítico" do pedido e identificar qual dos microsserviços está a causar lentidão.

## Descrição do Fluxo

1.  **Início Assíncrono:**
    *   O Paciente envia o pedido. O Gateway valida a segurança (JWT/mTLS) e gera o rasto (`Trace-ID`).
    *   A API grava o evento `AppointmentCreatedEvent` e responde imediatamente com `202 Accepted`. O utilizador não fica bloqueado à espera das validações complexas.
    *   O envio para o Broker inclui padrões de resiliência (**Retry**) para garantir que a mensagem não se perde em caso de falha de rede momentânea.

2.  **Validação Distribuída:**
    *   O *Message Broker* distribui o evento.
    *   O **Patient Service** verifica se o paciente tem dívidas ou seguros ativos.
    *   O **Physician Service** verifica se o médico tem disponibilidade na agenda.
    *   Ambos publicam o resultado no Broker, mantendo o Trace-ID original.

3.  **Conclusão da Transação:**
    *   O *Scheduling Service* consome as respostas. Graças ao Event Sourcing, ele reconstrói o estado em memória para saber se já tem tudo o que precisa.
    *   Se ambas as validações forem positivas, grava o evento definitivo `AppointmentBookedEvent`.

4.  **Consistência Eventual (CQRS):**
    *   O evento final é consumido pelo *Query Side*, que atualiza a base de dados de leitura para que a consulta apareça finalmente na lista do utilizador como "Confirmada".