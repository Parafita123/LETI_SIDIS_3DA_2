Fluxo de Agendamento: Saga (Coreografia) + CQRS

Este diagrama ilustra o processo de agendamento de uma consulta médica numa arquitetura de microsserviços, utilizando o padrão Saga (Coreografia) para gerir a transação distribuída e o padrão CQRS para separar a escrita da leitura.

Etapas do Processo
1. Início do Agendamento (Command Side)
   Comando: O Paciente envia um pedido POST para o API Gateway.
   Persistência Inicial: O Scheduling Command API recebe o pedido e grava a consulta na base de dados de escrita (Scheduling DB) com o estado PENDING (Pendente).
   Resposta Imediata: Para não bloquear o utilizador, o sistema retorna imediatamente um 202 Accepted, indicando que o processo começou, mas ainda não terminou.
   Evento Gatilho: É publicado o evento AppointmentCreatedEvent no Message Broker.
2. Validação Distribuída (Saga Pattern)
   Os serviços validam o pedido em paralelo, reagindo ao evento de criação (Coreografia):
   Patient Service: Verifica se o paciente é válido (ex: tem seguros ativos, sem dívidas) e publica PatientValidatedEvent.
   Physician Service: Verifica a disponibilidade de horário do médico e publica PhysicianAvailabilityConfirmedEvent.
3. Conclusão da Transação
   O Scheduling Command API atua como o interessado final, escutando os eventos de sucesso dos validadores.
   Após receber ambas as confirmações, atualiza o estado da consulta na base de dados para CONFIRMED.
   Publica o evento final AppointmentBookedEvent.
4. Atualização de Leitura (CQRS)
   O Scheduling Query API consome o evento de confirmação (AppointmentBookedEvent).
   Os dados são estruturados e gravados na Consultations Read DB.
   Isto garante que as consultas de leitura futuras sejam rápidas e não sobrecarreguem a base de dados transacional, assegurando a Consistência Eventual.

Este diagrama é bastante parecido com o das US 7, 20.