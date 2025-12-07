Detalhes do Paciente: Acesso Seguro (CQRS Query Side)

Este diagrama detalha o fluxo de leitura de um paciente específico, com ênfase na segurança dos dados. Ilustra como o sistema impede que um médico aceda à ficha de um paciente com o qual não tem relação clínica, utilizando filtros na própria consulta à base de dados.

Fluxo do Processo
1. Segurança e Contexto (Gateway)
   Pedido: O Médico solicita a ficha de um paciente (GET /patients/{id}).
   Autenticação: O API Gateway valida o Token JWT.
   Identificação Segura: O Gateway extrai a claim physicianId do payload do token e injeta-a no cabeçalho X-Physician-ID. Isto garante que a identidade do médico é validada pelo servidor e não pode ser falsificada pelo cliente.
2. Consulta Condicional (Query Logic)
   O Patient Query API executa uma consulta à base de dados de leitura (Read DB) combinando dois critérios obrigatórios:
   O ID do paciente (_id).
   A presença do ID do médico na lista de permissões (relatedPhysicianIds).
3. Cenários de Resposta (Bloco Alt)
   O sistema reage de forma diferente dependendo do resultado da consulta:
   Sucesso (Autorizado):
   Se o paciente existe E o médico está associado a ele, a base de dados devolve o documento.
   O serviço converte os dados para um DTO e retorna 200 OK com o JSON.
   Falha (Não Autorizado ou Inexistente):
   Se o paciente não existe OU se o médico não tem permissão (não está na lista relatedPhysicianIds), a base de dados retorna null.
   O serviço responde com 404 Not Found.
   Nota de Segurança: Retornar 404 em vez de 403 (Forbidden) é uma prática comum para evitar a fuga de informação, não revelando sequer se o paciente existe no sistema.