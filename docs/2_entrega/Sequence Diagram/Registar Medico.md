Registo de Médico: Fluxo Assíncrono (Saga + CQRS)

Este diagrama demonstra o processo de criação de um novo médico no sistema. O fluxo utiliza o padrão Saga para coordenar a criação do perfil profissional e das credenciais de acesso em microsserviços distintos, e CQRS para propagar os dados para leitura.

Etapas do Processo
1. Início do Comando (Command Side)
   Ação: O Administrador envia os dados do médico (licença, especialidade, etc.) para o API Gateway.
   Estado Provisório: O Physician Command API grava o médico na base de dados de escrita com o estado PENDING_ID (Pendente de Identidade).
   Performance: O sistema retorna imediatamente 202 Accepted, libertando o administrador enquanto o processamento continua em segundo plano.
   Evento: É publicado o evento PhysicianRegistrationStarted.
2. Criação de Credenciais (Saga Pattern)
   A Saga garante a consistência distribuída entre os dados de negócio e os dados de autenticação:
   O Identity Service consome o evento inicial.
   Cria um utilizador no sistema de autenticação e atribui-lhe a Role DOCTOR.
   Publica o evento PhysicianIdentityCreated contendo o ID de autenticação gerado.
3. Consolidação do Registo
   O Physician Command API recebe a confirmação de que o login foi criado.
   Atualiza o registo do médico na base de dados, associando o ID de autenticação e alterando o estado para ACTIVE.
   Publica o evento final PhysicianRegistered.
4. Atualização de Leitura (CQRS)
   O Physician Query API escuta o evento final.
   A base de dados de leitura (Read DB) é atualizada com a nova ficha do médico, ficando imediatamente disponível para pesquisas e listagens.