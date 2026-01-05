# Fluxo de Leitura de Estatísticas (Query Side)

Este diagrama ilustra o processo de obtenção de dados analíticos (estatísticas de duração de consultas) no lado de Leitura (**Query Side**) da arquitetura CQRS. O foco principal deste fluxo não é a lógica de negócio complexa, mas sim a **Performance**, **Segurança** e **Resiliência** do sistema distribuído.

## Principais Componentes e Padrões

1.  **Segurança (Zero Trust & mTLS):**
    *   A autenticação externa é feita via **JWT** (validado no API Gateway).
    *   A comunicação interna entre o Gateway e a *Scheduling Query API* é protegida por **mTLS** (Mutual TLS), garantindo que apenas serviços autorizados dentro do cluster Kubernetes podem invocar a API.

2.  **Observabilidade (Distributed Tracing):**
    *   Assim que o pedido entra no sistema, o API Gateway gera um **Trace-ID** único (ex: `tr-999`).
    *   Este ID é propagado nos cabeçalhos HTTP (`X-Trace-Id`) para todos os serviços subsequentes.
    *   Isto permite monitorizar o tempo total da transação e identificar gargalos (bottlenecks) através de ferramentas como **Jaeger** ou **Zipkin**.

3.  **Resiliência (Resilience4j):**
    *   Como as consultas estatísticas podem ser pesadas para a base de dados, implementámos padrões de proteção na *Query API*:
        *   **TimeLimiter:** Define um tempo máximo (ex: 2s) para a base de dados responder. Se demorar mais, a thread é libertada imediatamente para não bloquear o servidor.
        *   **Bulkhead:** Limita o número de pedidos concorrentes simultâneos a esta rota, impedindo que um pico de tráfego de leitura afete outras partes do sistema.
    *   **Fallback:** Em caso de erro ou timeout, o sistema não "crasha"; retorna uma resposta degradada de forma controlada (ex: dados em cache ou mensagem de erro amigável).

## Descrição do Fluxo

1.  **Pedido Inicial:** O Administrador solicita a média de duração das consultas. O API Gateway valida as credenciais e injeta o contexto de rastreio (`Trace-ID`).
2.  **Monitorização:** A *Query API* reporta o início do processamento ("Start Span") à stack de observabilidade.
3.  **Proteção de Recursos:** Antes de contactar a base de dados, o framework **Resilience4j** verifica se há "slot" disponível (Bulkhead) e inicia o temporizador (Timeout).
4.  **Consulta à Base de Leitura (Read DB):** A API executa a query SQL/NoSQL na base de dados de leitura (uma *Projection* otimizada para queries rápidas, separada da escrita).
5.  **Resposta e Métricas:**
    *   Se a BD responder a tempo: Os dados são mapeados para DTO e retornados.
    *   Se ocorrer Timeout/Erro: O mecanismo de **Fallback** é ativado.
    *   No final, são enviadas métricas (latência, sucesso/erro) para o **Prometheus** para monitorização em tempo real.