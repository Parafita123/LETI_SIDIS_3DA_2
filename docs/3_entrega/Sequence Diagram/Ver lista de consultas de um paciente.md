# Fluxo de Detalhes do Paciente (GDPR & Resiliência)

Este diagrama detalha o processo de leitura de dados sensíveis de um paciente específico. Tratando-se de informação médica confidencial, a arquitetura prioriza a **Segurança (Zero Trust)**, a **Conformidade com o GDPR** (Princípio do "Need-to-know") e a **Auditoria** de acessos, além da performance de leitura.

## Decisões de Arquitetura e Segurança

1.  **Segurança e GDPR (Acesso Contextual):**
    *   **Gateway Enforcement:** A segurança não confia nos parâmetros enviados pelo cliente. O *API Gateway* extrai o `physicianId` diretamente do Token JWT assinado e injeta-o no cabeçalho `X-Physician-ID`.
    *   **Query Segura:** A consulta à base de dados exige sempre a correspondência dupla: o ID do paciente **E** a presença do ID do médico na lista `relatedPhysicianIds`. Isto garante matematicamente que um médico não consegue aceder à ficha de um paciente que não acompanha, impedindo fugas de dados.
    *   **Obscuridade (Security by Obscurity):** Se um médico tentar aceder a um paciente não autorizado, o sistema retorna **404 Not Found** (em vez de 403 Forbidden). Isto impede ataques de "Enumeração de IDs", onde um atacante tentaria descobrir quais pacientes existem no sistema baseando-se nos códigos de erro.

2.  **Auditoria e Observabilidade:**
    *   **Métrica de Negócio (`sensitive_data_viewed`):** Cada visualização de detalhes dispara uma métrica específica para o Prometheus. Isto permite criar alertas de segurança (ex: "Médico X visualizou 100 fichas em 5 minutos").
    *   **Tracing:** O `Trace-ID` (`tr-audit-99`) serve como prova forense de quem acedeu a que dados e quando.

3.  **Resiliência (Fail Fast):**
    *   **TimeLimiter (500ms):** Uma leitura por Chave Primária (`_id`) numa base NoSQL deve ser sub-milissegundo. Configurámos um timeout agressivo de 500ms. Se demorar mais, o sistema assume que há um problema infraestrutural e corta a conexão imediatamente, libertando recursos do servidor.

## Descrição do Fluxo

1.  **Início Seguro:** O médico solicita os detalhes. O Gateway valida o JWT, extrai a identidade do médico e inicia o rastreio.
2.  **Proteção:** A *Query API* aplica as políticas de resiliência e valida a comunicação mTLS.
3.  **Consulta:** A API executa `findOne` na projeção de leitura.
4.  **Cenários de Resposta:**
    *   **Sucesso:** Se o documento for encontrado e a relação médico-paciente for válida, os dados são retornados e o acesso é contabilizado nas métricas de auditoria.
    *   **Não Autorizado/Inexistente:** Se o documento não existir ou o médico não tiver permissão, retorna-se `null` da DB e `404` para o cliente, protegendo a privacidade do paciente.
    *   **Falha Técnica:** Se a base de dados não responder em 500ms, o *Resilience4j* lança uma exceção. O erro é logado e o cliente recebe um `503 Service Unavailable`, indicando uma falha transitória sem expor detalhes internos.