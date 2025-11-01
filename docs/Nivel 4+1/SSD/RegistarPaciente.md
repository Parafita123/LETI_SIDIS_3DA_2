Explicação do Diagrama de Sequência: Registo de Paciente

Este diagrama de sequência ilustra o processo completo para o registo de um novo paciente por um utilizador autenticado (ex: Recepcionista), numa arquitetura de microserviços segura.

O fluxo está dividido em duas fases principais e demonstra a aplicação de padrões de design modernos como API Gateway e autenticação via JSON Web Tokens (JWT).

Principais Componentes:

API Gateway: Atua como o único ponto de entrada para o sistema, responsável por encaminhar as requisições e validar a segurança.

IdentityService: Um microserviço centralizado, cuja única responsabilidade é gerir a identidade dos utilizadores, validar credenciais e emitir tokens JWT.

PatientService (Componentes Internos): Representado pelos seus componentes Controller, Service e Repository, este microserviço detém toda a lógica de negócio relacionada com os pacientes.

Descrição do Fluxo:

O processo é executado em duas fases distintas:

Fase 1: Autenticação e Obtenção do Token

O utilizador inicia o processo de login através do :Frontend, fornecendo as suas credenciais.

A requisição é enviada ao :API Gateway, que a encaminha para o :IdentityService.

O :IdentityService valida as credenciais. Se forem válidas, gera um token JWT — um "passe" digital seguro que comprova a identidade do utilizador — e devolve-o ao :Frontend.

Fase 2: Registo de Paciente (Ação Autorizada)

Com o token armazenado, o utilizador submete o formulário de registo de um novo paciente.

O :Frontend envia a requisição de registo (POST /patients) para o :API Gateway, incluindo o token JWT no cabeçalho Authorization.

O ponto de controlo de segurança ocorre no :API Gateway, que valida a assinatura e a validade do token. Se o token for válido, a requisição é considerada legítima.

O Gateway encaminha a requisição para o ponto de entrada do serviço de pacientes, o PatientController.

Dentro do microserviço, o fluxo segue o padrão de arquitetura em camadas:

O PatientController recebe os dados.

O PatientServiceImpl aplica a lógica de negócio (cria a entidade Patient a partir dos dados recebidos).

O PatientRepository persiste a nova entidade na base de dados :H2Database.

A confirmação da criação do recurso flui de volta pela mesma cadeia, culminando numa resposta HTTP 201 Created, que é enviada ao utilizador, confirmando o sucesso da operação.entado pelos seus componentes Controller, Service e Repository, este microserviço detém toda a lógica de negócio relacionada com os pacientes.

Este diagrama é bastante parecido com o das US 11