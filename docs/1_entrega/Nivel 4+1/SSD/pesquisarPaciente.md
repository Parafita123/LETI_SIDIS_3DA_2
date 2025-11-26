
Explicação do Diagrama de Sequência: Médico a Pesquisar os Seus Pacientes

Este diagrama de sequência ilustra um fluxo de trabalho de segurança crítica: como um Médico pesquisa pacientes por nome, garantindo que o sistema devolve apenas os pacientes que lhe estão associados.

O processo demonstra um padrão de design avançado em microserviços, onde a identidade do utilizador, extraída de um token JWT, é usada para filtrar e delimitar o acesso aos dados, reforçando a privacidade e a segurança.

Principais Componentes:

API Gateway: Funciona como um firewall inteligente. Ele não só valida a autenticidade do token, mas também extrai a identidade do médico (physician_id) e injeta-a de forma segura na requisição para os serviços internos.

IdentityService: Responsável por autenticar o Médico e emitir um token JWT que contém o seu identificador único (physician_id), que serve como prova da sua identidade.

PatientService (Componentes Internos): O microserviço que detém a lógica de negócio dos pacientes. Neste fluxo, ele recebe não só o termo de pesquisa, mas também o ID do médico que está a realizar a pesquisa.

Descrição do Fluxo:

O processo é executado em duas fases essenciais:

Fase 1: Autenticação do Médico

O Médico efetua o login no sistema através do :Frontend.

O :IdentityService valida as suas credenciais e emite um token JWT, que é "carimbado" com o seu physician_id.

Fase 2: Pesquisa de Pacientes (Ação Autorizada e Filtrada)

O Médico submete um termo de pesquisa (ex: "Silva") na interface do :Frontend.

A requisição (GET /patients?name=Silva) é enviada ao :API Gateway, juntamente com o token JWT.

O ponto central da segurança e do isolamento de dados ocorre aqui:

O :API Gateway valida o token.

Em seguida, ele extrai o physician_id diretamente do conteúdo do token.

O Gateway enriquece a requisição antes de a encaminhar, adicionando o physician_id como um cabeçalho HTTP (X-Physician-ID). Isto garante que o serviço de destino recebe a identidade do médico de uma fonte confiável (o Gateway), e não do cliente, prevenindo manipulação.

O PatientController recebe tanto o termo de pesquisa como o physician_id.

A lógica de negócio no PatientServiceImpl e PatientRepository é construída para usar ambos os parâmetros.

A consulta à base de dados (:H2Database) reflete esta regra de negócio: ela executa um JOIN entre as tabelas de pacientes e de consultas para encontrar pacientes que correspondam ao nome E que tenham uma consulta associada ao physician_id do médico que fez o pedido.

Como resultado, a lista devolvida ao :Frontend é automaticamente filtrada, contendo apenas os pacientes que o médico está autorizado a ver.

Este diagrama é bastante parecido com o das US 3, 14