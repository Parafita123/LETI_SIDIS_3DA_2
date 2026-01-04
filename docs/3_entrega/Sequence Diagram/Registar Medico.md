# Fluxo de Registo de Médico (Saga & Identity)

Este diagrama ilustra o processo distribuído de registo de um novo profissional clínico. A complexidade deste caso de uso reside na necessidade de sincronizar dois domínios distintos: o **Domínio Clínico** (Dados do médico, licença, especialidade) e o **Domínio de Identidade** (Credenciais de acesso, Roles, Autenticação).

## Arquitetura e Decisões de Design

1.  **Saga Distribuída (Integração com Identity Service):**
    *   A criação de um médico não é uma operação atómica local. É necessário criar o registo na base de dados do hospital e, **apenas se isso for válido**, criar as credenciais no serviço de Identidade (ex: Keycloak/Auth0).
    *   O diagrama demonstra o fluxo assíncrono: o `Physician Command API` não comunica diretamente com o `Identity Service`. Utiliza-se o *Message Broker* para desacoplar os serviços, garantindo que o sistema clínico não falha se o sistema de identidade estiver temporariamente indisponível.

2.  **Event Sourcing (Auditabilidade Imutável):**
    *   Em vez de guardar apenas o estado final ("Médico Ativo"), o sistema grava a sequência de eventos: `PhysicianCreatedEvent` (Intenção) -> `PhysicianActivatedEvent` (Conclusão) ou `PhysicianRegistrationRejectedEvent` (Falha).
    *   Isto é crucial para auditoria: permite saber exatamente quando o pedido foi feito e quando as credenciais foram geradas.

3.  **Observabilidade (Rastreio Cross-Service):**
    *   O **Trace-ID** (`tr-777`) é gerado no Gateway e propagado para o *Identity Service* via RabbitMQ.
    *   Isto permite correlacionar logs de serviços diferentes. Se um médico não for criado, o administrador consegue verificar se o erro ocorreu na validação da licença ou na criação do utilizador (ex: email duplicado).

4.  **Resiliência:**
    *   A publicação no Broker inclui uma **Retry Policy** (Resilience4j). Se o RabbitMQ estiver em baixo momentaneamente, o serviço tenta reenviar o evento antes de desistir.

## Descrição das Etapas

1.  **Início (Intenção):** O Admin envia os dados. O Gateway valida a segurança e inicia o rastreio. O *Command API* persiste a "intenção de criar" (`PhysicianCreatedEvent`) no *Event Store* e retorna `202 Accepted` para não bloquear o Admin.
2.  **Processamento Externo:** O *Identity Service* consome o evento. Tenta criar o utilizador com a Role `DOCTOR`.
    *   **Sucesso:** Emite `PhysicianIdentityCreatedEvent`.
    *   **Erro:** Emite `PhysicianIdentityFailedEvent`.
3.  **Consolidação:** O *Command API* escuta a resposta da Identidade.
    *   Faz o **Rehydrate** do estado (lê eventos anteriores).
    *   Grava o evento final: `Activated` (Sucesso) ou `Rejected` (Rollback da Saga).
4.  **Projeção:** O *Query Side* atualiza a vista de leitura, tornando o médico visível nas pesquisas e associando-o ao seu novo ID de autenticação (`AuthID`).