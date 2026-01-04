# Fluxo de Registo de Paciente (Saga & Event Sourcing)

Este diagrama detalha o processo de criação de um novo paciente no sistema HAP. Devido à necessidade de criar simultaneamente o registo clínico e as credenciais de acesso (Identity Service), o processo é gerido como uma **Saga Distribuída**. Além disso, o uso de **Event Sourcing** garante que todo o histórico de tentativas de registo fica preservado para auditoria.

## Decisões Arquiteturais e Padrões

1.  **Event Sourcing e Auditoria:**
    *   Ao contrário de uma base de dados tradicional onde um erro poderia levar à remoção do registo (`DELETE`), aqui utilizamos um modelo *Append-Only*.
    *   Se o registo falhar (ex: e-mail já existente no sistema de identidade), o sistema grava um `PatientRegistrationRejectedEvent`. Isto garante um rasto de auditoria completo: sabemos quem tentou registar o paciente, quando, e porque falhou. Nenhuns dados são apagados.

2.  **Saga com Compensação (Transação Distribuída):**
    *   O registo envolve dois domínios: **Paciente** (Dados pessoais) e **Identidade** (Login/Auth).
    *   O *Patient Command API* atua como orquestrador implícito. Ele inicia o processo, aguarda a resposta assíncrona do *Identity Service* e, dependendo do resultado, avança para o estado `Activated` ou executa uma **Transação de Compensação** (marcando como `Rejected`).

3.  **Observabilidade (Traceability):**
    *   O registo de pacientes é crítico. O **Trace-ID** (`tr-555`) gerado no Gateway permite seguir o pedido através das filas de mensagens (*RabbitMQ*) e serviços externos.
    *   Isto é essencial para debugging: se um paciente reclamar que não recebeu o e-mail de ativação, o suporte consegue localizar exatamente onde o processo parou.

4.  **Segurança (Zero Trust):**
    *   O Gateway valida que apenas utilizadores com a Role `STAFF` (Rececionistas) podem iniciar este fluxo.
    *   A comunicação interna é protegida por **mTLS**, impedindo que serviços não autorizados injetem eventos de criação de pacientes.

## Descrição do Fluxo

1.  **Início (Comando):** A Rececionista envia os dados. O sistema valida o JWT, gera o Trace-ID e persiste o evento `PatientRegistrationStartedEvent`. O utilizador recebe imediatamente um `202 Accepted` (Assincronismo).
2.  **Passo de Identidade:** O *Identity Service* consome o evento e tenta criar as credenciais no provedor de autenticação (ex: Auth0/Keycloak).
    *   Publica `PatientIdentityCreatedEvent` em caso de sucesso.
    *   Publica `PatientIdentityFailedEvent` em caso de erro.
3.  **Consolidação e Compensação:**
    *   O *Patient Command API* recebe a resposta e **Reidrata** o estado (lê eventos passados para contexto).
    *   Se for sucesso: Grava `PatientActivatedEvent`.
    *   Se for falha: Grava `PatientRegistrationRejectedEvent` (Compensação lógica).
4.  **Atualização de Leitura:** Finalmente, o *Patient Query API* atualiza a base de dados de leitura para que o paciente fique (ou não) visível nas pesquisas, garantindo a Consistência Eventual.