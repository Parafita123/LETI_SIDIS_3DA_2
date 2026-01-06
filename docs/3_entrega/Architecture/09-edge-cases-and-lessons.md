# 09 – Edge Cases and Lessons Learned
## 0. Objetivo
Este documento descreve os principais edge cases analisados e testados durante o desenvolvimento da Parte 3 (P3), bem como as limitações conhecidas da solução e as lições aprendidas. O objetivo é demonstrar consciência dos riscos reais em sistemas distribuídos e justificar decisões arquiteturais tomadas.

---
## 1. Edge cases testados
## 1.1 Network partitions (falhas de comunicação)
Cenário:
- Interrupção da comunicação entre microserviços.
- Serviço remoto indisponível (ex.: physician-service desligado).
  Casos testados:
- Scheduling Service tenta validar physician com serviço indisponível.
- Clinical Records Service tenta validar consulta ou paciente com falha de rede.
  Comportamento observado:
- Timeouts ativados rapidamente.
- Retries limitados são executados.
- Circuit breaker abre após falhas consecutivas.
- O sistema devolve erro controlado sem bloquear outros fluxos.
  Conclusão:
- O sistema tolera partições de rede temporárias.
- Não ocorre falha em cascata.
- A recuperação é automática quando a ligação é restabelecida.
## 1.2 Falhas parciais em múltiplas instâncias
Cenário:
- Apenas uma instância de um microserviço falha.
  Casos testados:
- Desligar uma réplica do physician-service.
- Manter outras instâncias ativas.
  Comportamento observado:
- Load balancing encaminha requests para instâncias saudáveis.
- Apenas requests que atingem a instância falhada sofrem retry.
- O sistema continua operacional.
  Conclusão:
- A arquitetura suporta falhas parciais.
- A replicação por serviço aumenta a disponibilidade.
## 1.3 High load (carga elevada)
Cenário:
- Número elevado de requests concorrentes.
  Casos testados:
- Múltiplos POST /consultas em curto espaço de tempo.
- Leitura intensiva de listas (queries).
  Comportamento observado:
- Métricas de latência aumentam gradualmente.
- Bulkheads evitam saturação global.
- O sistema mantém resposta funcional, ainda que degradada.
  Conclusão:
- O sistema degrada de forma controlada.
- Não ocorre esgotamento total de recursos.
## 1.4 Mensagens duplicadas (AMQP)
Cenário:
- Reentrega de mensagens RabbitMQ devido a falhas no consumer.
  Casos testados:
- Simular erro no consumer após processamento parcial.
- Reentrega da mesma mensagem.
  Comportamento observado:
- Consumers idempotentes evitam duplicação de efeitos.
- Mensagens problemáticas são encaminhadas para DLQ.
  Conclusão:
- O sistema é tolerante a duplicação de mensagens.
- Não ocorre inconsistência grave nos dados.
## 1.5 Falhas durante Sagas
Cenário:
- Falha num step intermédio de uma Saga.
  Casos testados:
- Criação de consulta falha após validação parcial.
  Comportamento observado:
- Saga entra em estado de erro.
- Compensações são executadas.
- Estado final consistente.
  Conclusão:
- A Saga garante consistência mesmo sob falhas.
- O fluxo completo é rastreável via logs e tracing.
---
## 2. Edge cases não totalmente resolvidos
## 2.1 Partições prolongadas
Descrição:
- Falhas de rede de longa duração entre serviços.
  Limitação:
- Sagas podem acumular estados pendentes.
- Read models podem ficar desatualizados por mais tempo.
  Mitigação:
- Monitorização ativa.
- Intervenção manual em cenários extremos.
## 2.2 Picos extremos de carga
Descrição:
- Carga muito superior ao cenário esperado.
  Limitação:
- O sistema não implementa autoscaling avançado baseado em métricas customizadas.
  Mitigação:
- HPA básico ou escalonamento manual.
- Ajuste de limites de bulkhead.
## 2.3 Consistência eventual visível ao utilizador
Descrição:
- Pequeno atraso entre command e query (CQRS).
  Limitação:
- O utilizador pode não ver imediatamente o resultado de uma ação.
  Mitigação:
- Comunicação clara via UI.
- Reintentos de leitura.
## 2.4 Gestão manual de DLQ
Descrição:
- Mensagens em Dead Letter Queue requerem análise manual.
  Limitação:
- Não existe reprocessamento automático avançado.
  Mitigação:
- Runbook documentado.
- Ferramentas do broker.
---
## 3. Lições aprendidas
## 3.1 Sistemas distribuídos falham por defeito
- Falhas não são exceções, são esperadas.
- Timeouts e circuit breakers são essenciais.
## 3.2 Observabilidade é crítica
- Sem logs, métricas e tracing, debugging é impraticável.
- A observabilidade foi essencial para validar resiliência.
## 3.3 Consistência forte nem sempre é viável
- CQRS e Sagas implicam consistência eventual.
- É necessário aceitar e gerir essa realidade.
## 3.4 Simplicidade é uma vantagem
- Nem todos os edge cases devem ser resolvidos com complexidade excessiva.
- Algumas decisões foram conscientemente simplificadas para o contexto académico.
---
## 4. Conclusão
O sistema demonstra robustez perante diversos edge cases realistas de sistemas distribuídos. As limitações conhecidas são compreendidas, documentadas e justificadas face ao contexto e objetivos do projeto. A arquitetura resultante é resiliente, observável e defensável do ponto de vista técnico e académico.
