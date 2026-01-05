# Fluxo de Registo de Detalhes Clínicos (Saga & Event Sourcing)

Este diagrama ilustra o processo de gravação de dados clínicos (diagnósticos, receitas, notas) associados a uma consulta. Devido à natureza sensível e legal destes dados, a arquitetura utiliza **Event Sourcing** para garantir um histórico inalterável e uma **Saga Coreografada** para propagar o estado de conclusão da consulta.

## Decisões Arquiteturais Chave

1.  **Event Sourcing (Imutabilidade Legal):**
    *   Em sistemas médicos, nunca se deve sobrescrever (*overwrite*) um diagnóstico. Se um médico comete um erro, deve criar uma nova entrada de correção.
    *   O sistema implementa isto nativamente ao usar um *Event Store*. Em vez de um `UPDATE` na tabela, fazemos um **APPEND** do evento `ConsultationDetailsAddedEvent`. Isto cria um rasto de auditoria perfeito e imutável ("Audit Log") de todas as interações.

2.  **Saga Coreografada (Side-Effects):**
    *   O registo de detalhes clínicos tem um efeito colateral noutro domínio: significa que a consulta ocorreu efetivamente.
    *   Através do padrão **Coreografia**, o *Scheduling Service* escuta o evento de detalhes (`DetailsAddedEvent`) e transita automaticamente o estado da consulta para `COMPLETED`. Isto mantém os serviços de "Registos Clínicos" e "Agendamento" totalmente desacoplados.

3.  **Observabilidade Transversal:**
    *   Como um único clique do médico despoleta ações em três serviços diferentes (Command API, Scheduling Service, Query API), o **Trace-ID** (`tr-med-01`) é fundamental para monitorizar a saúde de toda a transação distribuída e detetar falhas de sincronização.

4.  **Segurança (Contexto do Médico):**
    *   O Gateway extrai o `physicianId` do Token JWT e injeta-o no cabeçalho. A *Command API* usa este ID durante a fase de **Reidratação** para garantir que apenas o médico responsável pela consulta pode adicionar notas à mesma.

## Descrição do Fluxo

1.  **Comando Seguro:** O médico submete os detalhes. O Gateway valida a identidade, gera o rasto (`Trace-ID`) e encaminha via mTLS.
2.  **Persistência (Event Sourcing):**
    *   A API carrega os eventos passados (`Load Stream`) para validar o estado atual.
    *   Grava o novo evento no *Event Store*.
    *   Retorna `202 Accepted` imediatamente, libertando o utilizador.
3.  **Processamento Paralelo (Async):**
    *   **Atualização de Estado (Saga):** O *Scheduling Service* consome o evento e marca a consulta como Concluída, emitindo o seu próprio evento `AppointmentCompletedEvent`.
    *   **Projeção (CQRS):** O *Query API* atualiza a base de dados de leitura (NoSQL), adicionando os novos detalhes ao documento do prontuário para que fiquem visíveis na interface do médico.