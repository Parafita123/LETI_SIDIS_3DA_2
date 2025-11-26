
Explicação do Diagrama de Sequência: Registo de Médico

Este diagrama de sequência detalha o fluxo de trabalho para um Administrador registar um novo médico no sistema, operando dentro de uma arquitetura de microserviços segura.

O processo demonstra um controlo de acesso baseado em papéis (role-based access control), onde apenas utilizadores com privilégios de Administrador podem executar esta operação, validada através de um token JWT.

Principais Componentes:

API Gateway: Ponto de entrada único que centraliza a segurança, validando o token e o papel (role) do utilizador antes de encaminhar a requisição.

IdentityService: Microserviço focado na gestão de identidades, responsável por autenticar o Administrador e emitir o token JWT que contém as suas permissões.

PhysicianService (Componentes Internos): O microserviço que encapsula toda a lógica de negócio para a gestão de médicos, incluindo o seu registo, consulta e atualização.

Descrição do Fluxo:

O fluxo é dividido em duas fases lógicas:

Fase 1: Autenticação do Administrador

O Administrador efetua o login no sistema através do :Frontend.

A requisição é enviada ao :API Gateway, que a delega ao :IdentityService para validação.

Após a validação bem-sucedida, o :IdentityService gera e retorna um token JWT. Este token contém informações cruciais, como o ID do utilizador e o seu papel (role: ADMIN).

Fase 2: Registo de Médico (Ação Autorizada)

O Administrador submete os dados do novo médico através do formulário no :Frontend.

O :Frontend envia a requisição de criação (POST /physicians), incluindo o token JWT no cabeçalho Authorization.

O ponto de controlo de autorização ocorre no :API Gateway. Ele valida a assinatura do token e, crucialmente, verifica se o papel (role) contido no token é "ADMIN", autorizando assim a operação.

Uma vez autorizado, o Gateway encaminha a requisição para o PhysicianController.

O PhysicianService executa a lógica de negócio para criar e persistir o novo médico na sua base de dados (:H2Database), seguindo o padrão Controller -> Service -> Repository.

O sucesso da operação é comunicado de volta através da cadeia de componentes com uma resposta HTTP 201 Created, e o Administrador é notificado no :Frontend.
