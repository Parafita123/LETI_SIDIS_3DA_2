
Explicação do Diagrama de Sequência: Relatório Mensal de Consultas

Este diagrama de sequência descreve o processo pelo qual um Administrador gera um relatório estatístico mensal de consultas, operando numa arquitetura de microserviços.

O fluxo destaca não apenas a segurança, garantindo que apenas um Administrador pode aceder a estes dados, mas também uma implementação eficiente que otimiza a consulta à base de dados.

Principais Componentes:

API Gateway: Ponto de entrada que assegura a autorização, verificando se o token JWT do utilizador contém o papel (role) de "ADMIN".

IdentityService: Responsável por autenticar o Administrador e emitir um token JWT que certifica a sua identidade e os seus privilégios.

AppointmentService (Componentes Internos): O microserviço que detém os dados das consultas e encapsula a lógica de negócio para agregar os dados e construir o relatório.

Descrição do Fluxo:

O processo é dividido em duas fases sequenciais:

Fase 1: Autenticação do Administrador

O Administrador autentica-se no sistema através do :Frontend.

O :IdentityService valida as credenciais e emite um token JWT, que inclui a informação de que o utilizador tem o papel de "ADMIN".

Fase 2: Geração do Relatório Mensal (Ação Autorizada)

O Administrador solicita o relatório através da interface, especificando o ano e o mês desejados.

O :Frontend envia uma requisição GET ao :API Gateway, passando o token JWT para autorização.

O :API Gateway interceta o pedido, valida o token e confirma que o utilizador tem o papel de "ADMIN", autorizando o acesso ao recurso.

A requisição é encaminhada para o AppointmentController, que delega a tarefa para a camada de serviço.

Ponto de Eficiência: Em vez de ir buscar todas as consultas da história para depois filtrar, o AppointmentServiceImpl invoca um método específico no repositório (findByYearAndMonth). Isto traduz-se numa consulta SQL otimizada (WHERE YEAR(...) AND MONTH(...)), que vai buscar à base de dados apenas os registos relevantes para o período solicitado, garantindo alta performance.

Com a lista de consultas do mês, o Service executa a lógica de agregação: conta o número total de consultas, as canceladas, as reagendadas, etc., e constrói um objeto ConsultasReportDTO com os resultados.

O DTO final é retornado através da cadeia de resposta (Controller -> Gateway -> Frontend), e os dados são mostrados ao Administrador.