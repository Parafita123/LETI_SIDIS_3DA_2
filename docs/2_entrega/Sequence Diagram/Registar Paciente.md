Registo de Paciente (Saga Assíncrona + CQRS)

Este diagrama descreve o processo de criação de um novo paciente no sistema. O fluxo destaca-se pelo uso de Persistência Poliglota (PostgreSQL para escrita, MongoDB para leitura) e pela coordenação distribuída entre dados demográficos e credenciais de acesso via padrão Saga.

Etapas do Processo
1. Início do Comando (Write Side)
   Ação: O Recepcionista envia os dados do paciente via POST através do API Gateway.
   Persistência Relacional: O Patient Command API grava o registo na Write DB (PostgreSQL) com o estado PENDING. O uso de SQL aqui garante integridade referencial estrita para os dados "master".
   Assincronismo: O sistema responde imediatamente com 202 Accepted, indicando que o pedido foi aceite, e publica o evento PatientRegistrationStarted no RabbitMQ.
2. Criação de Identidade (Saga Pattern)
   O Identity Service reage ao evento de início.
   Cria o utilizador no provedor de identidade (ex: Keycloak ou Auth0) para gerir a autenticação futura do paciente.
   Publica o evento PatientIdentityCreated com o ID de autenticação gerado.
3. Finalização do Comando
   O Patient Command API recebe a confirmação da identidade.
   Atualiza o registo no PostgreSQL, associando o authId e alterando o estado para ACTIVE.
   Emite o evento final PatientRegistered.
4. Sincronização de Leitura (CQRS)
   Atualização: O Patient Query API consome o evento de registo completo.
   Persistência NoSQL: Grava ou atualiza (UPSERT) uma visão desnormalizada do paciente na Read DB (MongoDB).
   Objetivo: O uso de MongoDB no lado da leitura permite que a aplicação recupere o perfil completo do paciente (incluindo dados aninhados) numa única operação rápida, sem necessidade de joins complexos.