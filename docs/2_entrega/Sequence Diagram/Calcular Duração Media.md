Consulta de Estatísticas: Duração Média por Médico (CQRS Query Side)

Este diagrama detalha o fluxo de leitura de dados agregados, correspondendo à parte Query do padrão CQRS. Foca-se na obtenção de relatórios estatísticos por um utilizador com privilégios administrativos.

Fluxo do Processo
1. Pedido Seguro e Autenticação
   Ação: O Administrador envia um pedido GET para o endpoint /stats/average-duration.
   Segurança: O pedido inclui um cabeçalho de autorização com um Token JWT.
   Gateway: O API Gateway atua como guardião, validando a assinatura do token e verificando se o utilizador possui a Role ADMIN. Apenas administradores podem aceder a estes dados sensíveis.
2. Processamento da Query
   Após a validação, o Gateway encaminha o pedido para o microsserviço responsável pelas leituras: Scheduling Query API.
   Ao contrário do fluxo de escrita (Command), este serviço não processa regras de negócio complexas nem altera estados. A sua única função é recuperar dados de forma eficiente.
3. Acesso aos Dados (Read DB)
   O serviço consulta a Scheduling Read DB (implementada em H2 neste exemplo).
   É executada uma operação de agregação (cálculo de médias de duração) diretamente na base de dados.
   Nota de Arquitetura: Esta base de dados é otimizada para leitura e está separada da base de dados transacional (Write DB), garantindo que pedidos de relatórios pesados não afetam a performance de novos agendamentos.
4. Resposta
   Os dados brutos da base de dados são mapeados para um objeto de transferência (DTO).
   O sistema retorna um JSON com a tabela de estatísticas e o código HTTP 200 OK.