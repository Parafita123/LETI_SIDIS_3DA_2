Explicação do Diagrama de Sequência: Duração Média de Consulta por Médico

Este diagrama de sequência descreve o processo pelo qual um Administrador obtém uma estatística de negócio: a duração média das consultas, agrupada por cada médico.
O fluxo ilustra uma funcionalidade de Business Intelligence (BI) ou de análise de dados, onde o acesso é restrito a utilizadores com privilégios administrativos, garantido através de um sistema de autenticação e autorização baseado em tokens JWT e papéis (roles).

Principais Componentes:

API Gateway: Ponto de entrada que atua como barreira de segurança, validando o token do utilizador e verificando se ele possui o papel (role) de "ADMIN" necessário para aceder a este recurso.

IdentityService: Responsável por autenticar o Administrador e emitir o token JWT que certifica a sua identidade e os seus privilégios administrativos.

AppointmentService (Componentes Internos): O microserviço que não só armazena os dados das consultas, mas também contém a lógica de negócio para processar e agregar esses dados brutos, transformando-os em informação estatística útil.

Descrição do Fluxo:

O processo é dividido em duas fases lógicas:

Fase 1: Autenticação do Administrador

O Administrador efetua o login no sistema.

O :IdentityService valida as suas credenciais e emite um token JWT que contém, no seu interior, a informação de que este utilizador tem o papel (role) de "ADMIN".

Fase 2: Obtenção da Estatística (Ação Autorizada)

O Administrador solicita a estatística de duração média através de uma interface no :Frontend.

A requisição (GET /appointments/stats/average-duration) é enviada ao :API Gateway, com o token JWT para autorização.

O :API Gateway interceta o pedido e executa a verificação de autorização: ele valida o token e confirma que o papel (role) do utilizador é "ADMIN". Apenas se esta condição for satisfeita, a requisição é permitida.

A requisição é encaminhada para o AppointmentController, que delega a tarefa para a camada de serviço.

O AppointmentServiceImpl executa a lógica de agregação em memória:

Primeiro, ele vai buscar à base de dados (:H2Database) a lista completa de todas as consultas (findAll).

Em seguida, ele processa esta lista: filtra as consultas relevantes (ex: apenas as completadas), agrupa-as por cada médico e, finalmente, calcula a média das durações para cada grupo.

O resultado final, um mapa que associa cada médico à sua duração média de consulta (Map<String, Double>), é retornado através da cadeia de resposta.

O :Frontend recebe os dados e apresenta a estatística ao Administrador, geralmente numa tabela ou gráfico.

Este diagrama é bastante parecido com o das US 17.
