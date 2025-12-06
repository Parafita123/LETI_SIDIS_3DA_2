Atualização e Cancelamento de Consultas (Saga + CQRS)

Este diagrama ilustra como o sistema gere as alterações ao ciclo de vida de uma consulta (reagendamento ou cancelamento). Ambas as operações seguem um modelo assíncrono para garantir alta disponibilidade e baixa latência, delegando a consistência dos dados para o processamento de eventos em segundo plano.

Pré-requisitos de Segurança

Todas as operações exigem que o utilizador (Paciente ou Administrador) esteja autenticado via Token JWT. O API Gateway e o Command API validam as permissões antes de processar qualquer alteração (ex: garantir que um paciente apenas altera as suas próprias consultas).

Cenário A: Reagendamento (Saga Pattern)

A alteração de data/hora é tratada como uma transação distribuída (Saga), pois requer uma nova validação de disponibilidade no serviço externo (Physician Service).

Pedido Inicial: O utilizador envia um pedido PUT com a nova data.

Estado Transitório:

O Scheduling Command API valida o pedido e atualiza o estado da consulta na base de dados de escrita para PENDING_RESCHEDULE.

O sistema retorna imediatamente 202 Accepted, libertando o cliente enquanto o processamento continua.

Validação Externa (Coreografia):

É publicado o evento AppointmentRescheduleStarted.

O Physician Service consome este evento e verifica a disponibilidade do médico para o novo horário.

Conclusão:

Se o horário estiver livre, é emitido um evento de confirmação.

O Command API finaliza a operação alterando o estado para CONFIRMED e a data para o novo valor.

Cenário B: Cancelamento (Event-Driven)

O cancelamento é um fluxo mais direto, focado na libertação de recursos.

Alteração de Estado: O Command API recebe o pedido DELETE, valida as regras de negócio e marca a consulta como CANCELLED na base de dados de escrita.

Resposta: Retorna-se imediatamente 202 Accepted ao utilizador.

Notificação: É publicado o evento AppointmentCancelled, que servirá para notificar o médico e libertar a vaga no sistema.

Sincronização de Leitura (CQRS)

Em ambos os cenários, a consistência entre a base de dados de escrita e a de leitura é garantida através de eventos:

O Scheduling Query API subscreve os eventos de conclusão (AppointmentUpdated e AppointmentCancelled).

A Scheduling Read DB é atualizada (ou o registo é removido/arquivado) para refletir a alteração.

Isto assegura a consistência eventual, garantindo que listagens e relatórios futuros apresentem os dados corretos.