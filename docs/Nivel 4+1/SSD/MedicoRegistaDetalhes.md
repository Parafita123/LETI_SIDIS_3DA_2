Explicação do Diagrama de Sequência: Médico a Registar Detalhes de uma Consulta

Este diagrama de sequência detalha uma operação crítica de negócio: o processo pelo qual um Médico regista informações pós-consulta (como diagnóstico, tratamento, etc.) para um atendimento específico.

O fluxo é desenhado para garantir não apenas a autenticação do médico, mas também a autorização a nível de registo, assegurando que um médico só pode modificar ou adicionar detalhes a uma consulta que lhe pertence.

Principais Componentes:

API Gateway: Ponto de entrada que valida o token JWT e extrai a identidade do médico (physician_id), injetando-a de forma segura na requisição para os serviços internos.

IdentityService: Responsável por autenticar o Médico e emitir o token JWT que prova a sua identidade.

AppointmentRecordService (Componentes Internos): O microserviço que armazena os registos detalhados das consultas e, crucialmente, contém a lógica de negócio para validar a "posse" de uma consulta antes de permitir uma escrita.

Descrição do Fluxo:

O processo é executado em duas fases sequenciais:

Fase 1: Autenticação do Médico

O Médico efetua o login no sistema através do :Frontend.

O :IdentityService valida as suas credenciais e retorna um token JWT que contém o seu physician_id único.

Fase 2: Registo dos Detalhes da Consulta (Ação Autorizada e Validada)

O Médico submete o formulário com os detalhes da consulta no :Frontend.

A requisição (POST /appointment-records/{id}/details) é enviada ao :API Gateway, com o token JWT para autorização.

O Gateway valida o token, extrai o physician_id e encaminha a requisição para o AppointmentRecordController.

O ponto central da segurança e integridade dos dados ocorre aqui, dentro do AppointmentRecordServiceImpl:

Primeiro, o serviço vai à base de dados (:H2Database) buscar o registo original da consulta, usando o ID recebido na URL.

Em seguida, ele executa uma verificação crítica: compara o ID do médico que veio do token (id_do_medico) com o ID do médico que está guardado nesse registo de consulta.

Esta verificação garante que um médico não pode, acidentalmente ou maliciosamente, registar detalhes numa consulta que não lhe pertence.

Apenas se a verificação de posse for bem-sucedida, o serviço prossegue com a operação de escrita, guardando os novos detalhes na base de dados.

A operação termina com uma resposta HTTP 201 Created, confirmando ao médico que os detalhes foram registados com sucesso.