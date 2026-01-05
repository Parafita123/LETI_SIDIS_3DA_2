# Fluxo de Atualização e Cancelamento (Command Side)

Este diagrama detalha como o sistema gere modificações de estado complexas (Reagendamento e Cancelamento) utilizando **Event Sourcing** e o padrão **Saga (Coreografia)**. Ao contrário de uma arquitetura tradicional baseada em *CRUD*, aqui nenhuma informação é sobrescrita; todas as mudanças são preservadas como uma sequência de eventos imutáveis.

## Conceitos Arquiteturais Chave

1.  **Event Sourcing & Reidratação (Rehydrate):**
    *   A base de dados de escrita (*Write DB*) não guarda o estado atual da consulta. Guarda apenas o histórico de eventos.
    *   Sempre que é necessário validar uma regra de negócio (ex: "Posso cancelar esta consulta?"), o serviço executa o processo de **Reidratação**: carrega todos os eventos passados desse ID (`Load History`) e reconstrói o estado em memória.
    *   As alterações de estado são feitas através de **APPEND** de novos eventos (ex: `RescheduleRequestedEvent`, `AppointmentCancelledEvent`).

2.  **Saga Coreografada (Cenário A - Reagendar):**
    *   O reagendamento não é atómico. Requer confirmação externa (Disponibilidade do Médico).
    *   O *Scheduling Service* inicia o processo e "pausa" logicamente, retornando `202 Accepted` ao utilizador.
    *   O *Physician Service* reage ao evento, valida o novo horário e emite uma confirmação. A Saga só termina quando o *Scheduling Service* processa essa confirmação e emite o evento final `AppointmentRescheduledEvent`.

3.  **Processamento Orientado a Eventos (Cenário B - Cancelar):**
    *   O cancelamento é um evento destrutivo que deve propagar-se por todo o sistema.
    *   Ao emitir `AppointmentCancelledEvent`, múltiplos serviços reagem em paralelo: a *Read DB* remove a consulta da vista do utilizador e o *Physician Service* liberta o horário na agenda do médico.

4.  **Observabilidade e Segurança:**
    *   **Trace-ID:** IDs únicos (ex: `tr-456`, `tr-789`) acompanham o pedido desde o Gateway até ao processamento assíncrono no Message Broker, permitindo correlacionar logs distribuídos.
    *   **mTLS:** Garante que apenas o Gateway pode invocar comandos de modificação na API.

## Descrição dos Cenários

### Cenário A: Reagendar (Complexo)
1.  **Pedido:** O Paciente solicita a alteração da data via `PUT`. O Gateway gera o Trace-ID `tr-456`.
2.  **Validação:** A *Command API* reconstrói o estado (Rehydrate) para garantir que a consulta existe e não está concluída.
3.  **Início da Saga:** É gravado e publicado o evento `RescheduleRequestedEvent`. O utilizador recebe resposta imediata (`202`).
4.  **Coreografia:** O *Physician Service* verifica a agenda. Se houver vaga, publica `PhysicianAvailabilityConfirmed`.
5.  **Conclusão:** A *Command API* consome a confirmação e grava o evento final `AppointmentRescheduledEvent`. A base de leitura é atualizada assincronamente.

### Cenário B: Cancelar (Direto)
1.  **Pedido:** O Paciente solicita o cancelamento via `DELETE` (Trace-ID `tr-789`).
2.  **Persistência:** Após validar que o cancelamento é permitido (via histórico), a API grava o evento `AppointmentCancelledEvent`.
3.  **Propagação:** O evento é publicado no Broker.
4.  **Efeitos Colaterais:** O *Scheduling Query API* marca a consulta como cancelada (ou remove-a) e o *Physician Service* torna o horário disponível novamente para outros pacientes.