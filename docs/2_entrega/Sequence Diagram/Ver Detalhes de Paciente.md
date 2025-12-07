Ver Detalhes do Paciente: Vista do Médico (CQRS Query Side)

Este diagrama ilustra o fluxo de leitura para obter a ficha completa de um paciente específico. O foco principal deste cenário é a privacidade dos dados e a implementação de segurança ao nível da consulta (Row-Level Security lógica).

Fluxo do Processo
1. Contexto de Segurança (Gateway)
   Pedido: O Médico solicita os detalhes de um paciente específico (GET /patients/{id}).
   Enriquecimento: O API Gateway valida o Token JWT e extrai o identificador do médico (physicianId).
   Propagação: Este ID é injetado num cabeçalho HTTP interno (X-Physician-ID) antes de o pedido ser encaminhado. Isto permite que o microsserviço saiba quem está a fazer o pedido sem ter de reprocessar a autenticação.
2. Consulta Segura (Query Logic)
   O Patient Query API recebe o pedido e consulta a Patient Read DB.
   Segurança via Filtro: A query à base de dados impõe uma condição estrita: procura um documento onde o ID do paciente corresponda ao solicitado E onde o ID do médico esteja na lista de relatedPhysicianIds.
   Se o médico não tiver uma relação clínica prévia com este paciente, a base de dados não devolve resultados (mesmo que o paciente exista). Isto impede que médicos consultem fichas de pacientes que não são seus.
3. Resposta
   Se a relação for validada, o documento do paciente é retornado.
   Os dados são mapeados para um DTO de detalhes e enviados como JSON (200 OK) para o frontend.

Este diagrama é bastante parecido com o das US 2, 10 e 14.