Explicação do Diagrama de Sequência: Paciente a Ver os Seus Registos de Consultas

Este diagrama de sequência demonstra o fluxo seguro pelo qual um Paciente acede à sua própria lista de registos de consultas. Este é um caso de uso fundamental numa arquitetura de microserviços, pois ilustra como a privacidade e o isolamento dos dados do utilizador são garantidos através da identidade contida num token JWT.

Principais Componentes:

API Gateway: Ponto de entrada que não só valida a autenticação do utilizador, mas também desempenha um papel ativo na segurança ao extrair a identidade do paciente (patient_id) do token.

IdentityService: Microserviço responsável por autenticar o Paciente e emitir um token JWT que o identifica de forma única e segura no sistema.

AppointmentRecordService (Componentes Internos): O microserviço que detém a lógica de negócio e os dados relativos aos registos de consultas.

Descrição do Fluxo:

O processo é dividido em duas fases principais:

Fase 1: Autenticação do Paciente

O Paciente inicia a sessão no sistema através do :Frontend, fornecendo as suas credenciais.

A requisição de login é processada pelo :IdentityService, que valida o utilizador e gera um token JWT.

Crucialmente, este token é "carimbado" com o patient_id do utilizador, servindo como a sua identidade digital para todas as interações futuras com o sistema.

Fase 2: Obtenção dos Registos de Consultas (Ação Autorizada e Filtrada)

O Paciente navega para a secção "Meus Registos" no :Frontend.

O :Frontend envia uma requisição GET para um endpoint específico (ex: /appointment-records/mine), incluindo o token JWT do paciente.

O ponto central da segurança ocorre no :API Gateway. Ele primeiro valida o token e depois extrai o patient_id do seu conteúdo.

O Gateway enriquece a requisição antes de a encaminhar para o AppointmentRecordController, adicionando o patient_id extraído como um cabeçalho seguro (ex: X-Patient-ID). Desta forma, o microserviço de destino não precisa de descodificar tokens, ele simplesmente confia na identidade fornecida pelo Gateway.

O AppointmentRecordService recebe a requisição e usa o patient_id fornecido para construir a sua lógica de negócio.

A camada de persistência (Repository) executa uma consulta à base de dados que é explicitamente filtrada por este patient_id (WHERE patient_id = ...). Isto garante o isolamento dos dados, tornando tecnicamente impossível que os registos de um paciente sejam acidentalmente expostos a outro.

A lista de registos, pertencente exclusivamente àquele paciente, é retornada ao :Frontend e exibida.

Este diagrama é bastante parecido com o das US 7, 20.