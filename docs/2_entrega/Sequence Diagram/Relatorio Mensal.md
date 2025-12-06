Relatório Mensal de Consultas (CQRS Query Side)

Este diagrama representa um fluxo de leitura analítica, onde o Administrador solicita dados estatísticos sobre as consultas realizadas num determinado mês. O cenário tira partido da arquitetura CQRS para realizar operações de agregação pesadas sem impactar o desempenho das operações de escrita (agendamentos em tempo real).

Fluxo do Processo
1. Segurança e Controlo de Acesso
   Pedido: O Administrador faz um pedido GET para obter o relatório, indicando o ano e o mês.
   Gateway: O API Gateway interceta o pedido para validar o Token JWT. É verificado especificamente se o utilizador possui a Role ADMIN, garantindo que informações sensíveis do negócio não são expostas a utilizadores comuns.
2. Processamento da Query (Agregação)
   O pedido é encaminhado para o Scheduling Query API.
   Lógica de Leitura: O serviço não se limita a buscar linhas simples. Executa uma operação de agregação na Scheduling Read DB (H2).
   A Operação: A base de dados filtra os registos pelo período solicitado e agrupa-os por estado ($group: { _id: "$status" }), contando o número de ocorrências. Isto permite saber rapidamente quantas consultas foram "Confirmadas", "Canceladas" ou ficaram "Pendentes".
3. Resposta Estruturada
   Os resultados brutos da agregação são convertidos num DTO (Data Transfer Object) específico para relatórios.
   O cliente recebe um JSON estruturado (ex: { "confirmed": 150, "cancelled": 5 }), pronto a ser visualizado em gráficos ou tabelas no dashboard administrativo.