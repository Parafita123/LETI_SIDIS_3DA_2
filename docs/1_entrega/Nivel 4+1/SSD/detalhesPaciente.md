Explicação do Diagrama de Sequência: Médico a Ver Detalhes de um Paciente

Este diagrama de sequência ilustra o processo pelo qual um Médico acede aos detalhes de um paciente específico. O fluxo foi desenhado com um foco rigoroso na segurança e privacidade, garantindo que um médico só possa visualizar informações de pacientes com os quais tem uma relação profissional estabelecida.

Esta validação é feita de forma transparente para o utilizador, utilizando a identidade do médico contida no seu token JWT para filtrar o acesso aos dados.

Principais Componentes:

API Gateway: Ponto de entrada que valida o token JWT e extrai a identidade do médico (physician_id), passando-a de forma segura para os serviços internos.

IdentityService: Responsável por autenticar o Médico e emitir um token JWT que o identifica de forma única no sistema.

PatientService (Componentes Internos): Microserviço que gere os dados dos pacientes e contém a lógica de negócio para validar a permissão de acesso de um médico a um registo de paciente específico.

Descrição do Fluxo:

O processo é dividido em duas fases lógicas:

Fase 1: Autenticação do Médico

O Médico efetua o login no sistema.

O :IdentityService valida as suas credenciais e emite um token JWT que inclui o seu identificador único (physician_id).

Fase 2: Obtenção dos Detalhes do Paciente (Ação Autorizada e Validada)

O Médico solicita a visualização dos detalhes de um paciente através do :Frontend, fornecendo o ID do paciente.

A requisição (GET /patients/{id_do_paciente}) é enviada ao :API Gateway, com o token JWT para autorização.

O ponto central da segurança e da validação de acesso ocorre aqui:

O :API Gateway valida o token e extrai o physician_id do seu conteúdo.

A requisição é encaminhada para o PatientController, contendo tanto o ID do paciente a ser consultado (da URL) como o ID do médico que está a fazer o pedido (do cabeçalho injetado pelo Gateway).

O PatientServiceImpl recebe ambos os IDs e delega a responsabilidade da validação de acesso à camada de persistência.

A validação de autorização é executada diretamente na consulta à base de dados. O PatientRepository constrói uma consulta SQL que faz um JOIN entre as tabelas de pacientes e de consultas. A cláusula WHERE da consulta impõe duas condições simultaneamente:

O ID do paciente deve corresponder ao solicitado.

E deve existir uma consulta que ligue este paciente ao physician_id do médico que fez o pedido.

A base de dados só retornará um resultado se ambas as condições forem verdadeiras. Se o médico não tiver nenhuma ligação com o paciente, a consulta não encontrará registos, e o acesso é efetivamente negado.

Se a autorização for confirmada, os dados do paciente são devolvidos ao :Frontend e exibidos ao Médico.

Este diagrama é bastante parecido com o das US 2, 10 e 14.