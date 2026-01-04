# Fluxo de Relatório Mensal (Resiliência & Observabilidade)

Este diagrama ilustra o processo de geração de relatórios estatísticos (Agregações) na arquitetura CQRS. Diferente de uma leitura simples por ID, este tipo de operação consome muitos recursos da base de dados. Por isso, a arquitetura foca-se na **Proteção do Sistema** contra sobrecarga (Resource Exhaustion) e na **Monitorização** do desempenho.

## Destaques da Implementação

1.  **Proteção e Resiliência (Resilience4j):**
    *   **TimeLimiter (3000ms):** Consultas de agregação (como `sum`, `group by`) podem bloquear threads por muito tempo. Configurámos um limite estrito de 3 segundos. Se a base de dados não responder nesse tempo, o pedido é abortado imediatamente. Isto aplica o padrão **Fail Fast**, impedindo que a API fique "pendurada" à espera da base de dados, o que poderia causar um efeito dominó e derrubar o serviço.
    *   **Circuit Breaker:** Embora não visível no fluxo de uma única chamada, está configurado para "abrir" (bloquear novos pedidos) se a taxa de falhas ou timeouts exceder 50%, dando tempo à base de dados para recuperar.

2.  **Base de Dados de Leitura (Mongo/Elastic):**
    *   No padrão CQRS, a *Read DB* é otimizada para consultas. Utilizamos uma base orientada a documentos (como MongoDB ou ElasticSearch) que lida com agregações complexas muito mais rapidamente do que a base relacional usada na escrita.

3.  **Observabilidade de Negócio e Técnica:**
    *   **Métricas (Prometheus):** Registamos métricas explícitas como `report_generated_success` e `report_timeout_error`. Isto permite criar dashboards para visualizar "Quantos relatórios são gerados por hora?" ou "Qual a taxa de erro nos relatórios?".
    *   **Tracing:** O `Trace-ID` (`tr-reports-01`) permite identificar exatamente qual a query que causou lentidão.

4.  **Segurança (RBAC & mTLS):**
    *   Sendo uma operação que expõe dados agregados sensíveis, o Gateway impõe validação estrita de Role (**ADMIN**).
    *   O uso de **mTLS** protege a comunicação interna.

## Descrição do Fluxo

1.  **Pedido Seguro:** O Administrador solicita o relatório. O Gateway valida a Role `ADMIN` e inicia o contexto de rastreio (`Trace-ID`).
2.  **Guarda (Resilience):** Antes de invocar a base de dados, a API verifica as políticas do *Resilience4j* (Timeouts e Circuit Breakers).
3.  **Agregação:** A API envia a query de agregação (`$match`, `$group`) para a base de leitura.
4.  **Cenários de Resposta:**
    *   **Sucesso:** Se a DB responder dentro de 3s, os dados são mapeados, a métrica de sucesso é incrementada e o JSON é devolvido.
    *   **Timeout:** Se exceder 3s, é lançada uma `TimeoutException`. O sistema regista o erro, incrementa a métrica de falha e retorna uma resposta `504 Gateway Timeout` (ou dados em cache via *Fallback*), protegendo a integridade do serviço.