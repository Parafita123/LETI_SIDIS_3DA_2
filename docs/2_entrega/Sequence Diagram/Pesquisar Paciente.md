Pesquisa de Pacientes: Vista do Médico (CQRS Query Side)

Este diagrama detalha o fluxo de leitura para a pesquisa de pacientes, focando-se na segurança e na privacidade dos dados. Representa o lado Query do padrão CQRS, otimizado para consultas rápidas e filtradas.

Segurança e Contexto

Autenticação: O pedido inicia-se com o Médico autenticado a enviar um pedido GET.

Enriquecimento do Pedido: O API Gateway não serve apenas de proxy; ele desempenha um papel ativo na segurança. Ele extrai o physicianId do Token JWT e injeta-o num cabeçalho HTTP interno (X-Physician-ID) antes de encaminhar o pedido para o microsserviço.

Lógica de Consulta (Query Logic)

O Patient Query API recebe o pedido e executa uma consulta à base de dados de leitura (Read DB).

Filtro de Privacidade: A consulta à base de dados aplica dois filtros simultâneos:

Critério de Texto: O nome do paciente (parcial ou completo).

Critério de Relacionamento: O physicianId.

Isto garante uma regra de negócio fundamental: um médico só consegue visualizar os pacientes com os quais tem uma relação clínica, impedindo o acesso indevido a outros registos.

Resposta Otimizada

Os dados brutos recuperados da base de dados são convertidos para objetos de transferência (DTOs) leves, contendo apenas a informação necessária para a listagem.

O sistema retorna a lista em formato JSON com o código 200 OK.

Este diagrama é bastante parecido com o das US 3, 14