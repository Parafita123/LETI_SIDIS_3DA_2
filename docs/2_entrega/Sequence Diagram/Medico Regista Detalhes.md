Registo de Detalhes da Consulta (CQRS Assíncrono)

Este diagrama ilustra o fluxo onde o Médico regista os dados clínicos (diagnóstico, receita, notas) após a realização de uma consulta. O processo utiliza uma abordagem Event-Driven com CQRS para separar a gravação dos dados da sua leitura, garantindo alta performance e desacoplamento.

Pré-requisitos de Segurança

O Médico deve estar autenticado com um Token JWT válido.

O sistema extrai e valida a claim physicianId para garantir uma regra de negócio crucial: apenas o médico responsável pela consulta pode adicionar registos clínicos à mesma.
1. Fluxo de Escrita (Command Side)
   Ação: O Médico submete os detalhes via POST através do API Gateway.
   Validação de Negócio: O ClinicalRecord Command API consulta a base de dados de escrita (Write DB) para confirmar se o médico autenticado corresponde ao ownerId do registo da consulta.
   Persistência e Evento:
   Após a validação, os detalhes são inseridos na base de dados transacional.
   Imediatamente, é publicado o evento AppointmentDetailsRecorded no Message Broker (RabbitMQ).
   Resposta: O sistema retorna 202 Accepted, confirmando que os dados foram guardados com segurança, sem obrigar o médico a esperar pela atualização de todo o sistema.
2. Sincronização de Leitura (Query Side)
   O microsserviço ClinicalRecord Query API escuta o evento AppointmentDetailsRecorded.
   A base de dados de leitura (Read DB) é atualizada (operação UPSERT) para incluir as novas informações no histórico clínico do paciente.
   Este mecanismo assegura a consistência eventual, garantindo que as futuras consultas ao histórico do paciente reflitam os dados inseridos, mantendo a base de dados de escrita leve e rápida.