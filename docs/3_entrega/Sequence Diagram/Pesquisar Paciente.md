# Fluxo de Pesquisa de Pacientes (Segura & Resiliente)

Este diagrama representa a arquitetura de leitura (**Query Side**) para a pesquisa de pacientes. Sendo uma operação frequente e que envolve dados sensíveis, o desenho da solução prioriza a **Privacidade (GDPR)**, a **Performance** e a **Proteção do Sistema** contra sobrecargas.

## Destaques da Implementação

1.  **Segurança e Privacidade (GDPR by Design):**
    *   **Enforcement no Gateway:** A segurança não depende apenas da API interna. O *API Gateway* extrai o `physicianId` do Token JWT e injeta-o obrigatoriamente no cabeçalho do pedido.
    *   **Filtro Obrigatório:** A query à base de dados inclui sempre `{ relatedPhysicianIds: 123 }`. Isto garante que um médico nunca consegue visualizar pacientes com os quais não tem relação clínica, mesmo que tente manipular os parâmetros da URL.
    *   **mTLS:** A comunicação entre o Gateway e a Query API é encriptada e autenticada mutuamente.

2.  **Resiliência (Resilience4j):**
    *   **TimeLimiter:** Foi configurado um timeout rigoroso de **1000ms**. Se a base de dados (NoSQL Projection) estiver degradada, o sistema aborta o pedido imediatamente em vez de manter a thread bloqueada ("Fail Fast").
    *   **Bulkhead:** Limita o número de pesquisas simultâneas. Se houver um pico de tráfego, o padrão *Bulkhead* impede que a funcionalidade de pesquisa consuma todos os recursos do cluster, protegendo outras funcionalidades críticas.

3.  **Observabilidade:**
    *   O **Trace-ID** (`tr-888`) é gerado na entrada e acompanha todo o ciclo de vida do pedido.
    *   São registadas métricas de negócio (sucesso vs. erro) e métricas técnicas (latência), permitindo a criação de alertas no Grafana se a taxa de erros na pesquisa aumentar.

## Descrição do Fluxo

1.  **Pedido e Contexto:** O médico inicia a pesquisa. O Gateway valida a identidade e gera o contexto de rastreio (`tr-888`).
2.  **Proteção:** Antes de executar a lógica, a *Query API* verifica as políticas de resiliência (concorrência e tempo).
3.  **Execução da Query:**
    *   A pesquisa é feita numa **Projeção de Leitura** (NoSQL), otimizada para velocidade, desacoplada da base de escrita.
    *   O filtro de segurança (`physicianId`) é aplicado nativamente na query.
4.  **Tratamento de Falhas:**
    *   **Caminho Feliz:** Retorna a lista de pacientes (DTOs limpos de dados técnicos).
    *   **Timeout/Erro:** Se o limite de 1000ms for excedido, a exceção é capturada. O sistema regista o erro nos logs (com o Trace-ID para debugging) e retorna uma resposta de erro controlada (503 ou lista vazia), evitando expor *stacktraces* ao cliente.