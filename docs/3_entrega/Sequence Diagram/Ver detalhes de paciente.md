# Fluxo de Relatório Mensal (Resiliência & Observabilidade)

Este diagrama ilustra o processo de geração de relatórios estatísticos (Agregações) na arquitetura CQRS. Diferente de uma leitura simples por ID, este tipo de operação consome muitos recursos da base de dados e de processamento. Por isso, o desenho da arquitetura foca-se na **Proteção do Sistema** contra sobrecarga (*Resource Exhaustion*) e na **Monitorização** do desempenho.

## Destaques da Implementação

1.  **Proteção e Resiliência (Resilience4j):**
    *   **TimeLimiter (3000ms):** Consultas de agregação (como `$group`, `$sum`) podem bloquear threads do servidor por muito tempo. Configurámos um limite estrito de 3 segundos. Se a base de dados não responder nesse tempo, o pedido é abortado imediatamente. Isto aplica o padrão **Fail Fast**, impedindo que a API fique "pendurada" à espera da base de dados, o que poderia causar um efeito dominó e tornar o serviço indisponível.
    *   **Circuit Breaker:** O sistema está configurado para "abrir o circuito" (rejeitar novos pedidos temporariamente) se a taxa de falhas ou timeouts exceder um limiar seguro (ex: 50%), dando tempo à base de dados para recuperar.

2.  **Base de Dados de Leitura (Mongo/Elastic):**
    *   No padrão CQRS, a *Read DB* é segregada e otimizada para consultas. Utilizamos uma base orientada a documentos (como MongoDB ou ElasticSearch) que lida com agregações complexas de forma muito mais eficiente do que a base relacional transacional.

3.  **Observabilidade de Negócio e Técnica:**
    *   **Métricas (Prometheus):** Registamos métricas explícitas como `report_generated_success` e `report_timeout_error`. Isto permite criar *dashboards* para visualizar tendências de uso e saúde do sistema.
    *   **Tracing:** O `Trace-ID` (`tr-reports-01`) gerado no Gateway permite identificar exatamente qual a query que causou lentidão ou erro.

4.  **Segurança (RBAC & mTLS):**
    *   Sendo uma operação que expõe dados agregados sensíveis do hospital, o Gateway impõe validação estrita de Role (**ADMIN**).
    *   O uso de **mTLS** protege a comunicação interna entre o Gateway e a API.

## Descrição do Fluxo

1.  **Pedido Seguro:** O Administrador solicita o relatório mensal. O Gateway valida a Role `ADMIN` e inicia o contexto de rastreio (`Trace-ID`).
2.  **Guarda (Resilience):** Antes de invocar a base de dados, a *Query API* verifica as políticas do framework *Resilience4j* (Timeouts e Circuit Breakers).
3.  **Agregação:** A API envia a query de agregação para a base de leitura.
4.  **Cenários de Resposta:**
    *   **Sucesso:** Se a BD responder dentro de 3s, os dados são mapeados para DTO, a métrica de sucesso é incrementada e o JSON é devolvido.
    *   **Timeout:** Se exceder 3s, é lançada uma exceção de Timeout. O sistema regista o erro no log e nas métricas, e retorna uma resposta `504 Gateway Timeout` (ou dados em cache via *Fallback*), protegendo a integridade do serviço e informando o cliente de forma graciosa.