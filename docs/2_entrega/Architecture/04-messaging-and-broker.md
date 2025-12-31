# 04 – Messaging and Broker (AMQP)

## 0. Objetivo do documento

Este documento descreve como é usado um **message broker AMQP** no sistema HAP para:

- Ligar os microserviços através de **eventos de domínio**.
- Suportar **CQRS** (atualização de read models por instância).
- Implementar **SAGAs** (coordenação de workflows distribuídos sem transações distribuídas).
- Melhorar **resiliência** e **escalabilidade** quando existem múltiplas instâncias de cada serviço.

---

## 1. Visão geral da arquitetura de messaging

### 1.1 Broker escolhido

- Broker: **RabbitMQ** (qualquer broker AMQP equivalente também funcionaria).
- Protocolo: **AMQP 0-9-1** / STOMP (dependendo da biblioteca utilizada no código).
- Todos os serviços que participam em eventos de domínio ligam-se ao broker como **produtores** e/ou **consumidores**.

### 1.2 Papel do broker no sistema

O broker é usado para:

1. **Propagação de eventos de domínio**
    - Ex.: `ConsultationScheduled`, `ConsultationCancelled`, `ConsultationRecordCreated`, `PatientRegistered`, `PhysicianRegistered`, etc.
    - Estes eventos são **fire-and-forget**: o produtor não fica à espera de resposta síncrona.

2. **Alimentar read models (CQRS)**
    - Cada instância consome eventos e atualiza o seu próprio **read model local** (database-per-instance para o lado de leitura).

3. **Implementar SAGAs**
    - Alguns serviços (por ex. Scheduling) atuam como **orchestrators**:
        - Publicam eventos de “começo de saga”.
        - Reagem a eventos de outros serviços para decidir se confirmam ou compensam uma operação.

4. **Desacoplar serviços**
    - Permite que serviços como Clinical Records, Reporting/Analytics, etc., reajam a alterações em Consultations, Patients e Physicians sem chamadas HTTP síncronas em cascata.

---

## 2. Modelo lógico de exchanges, filas e routing

### 2.1 Exchanges

Adotamos uma abordagem por domínio, com exchanges do tipo **topic**:

- `hap.patients` – eventos relacionados com Patient.
- `hap.physicians` – eventos relacionados com Physician.
- `hap.consultations` – eventos relacionados com Consultations (agendamento).
- `hap.records` – eventos relacionados com Consultation Records.
- Opcional: `hap.saga` – eventos “técnicos” de coordenação de SAGAs (se fizer sentido separar dos eventos de domínio).

Tipo de exchange: **topic**  
Motivos:

- Permite routing flexível por `routing-key`, por ex.:
    - `patient.registered`
    - `patient.updated`
    - `consultation.scheduled`
    - `consultation.cancellation.requested`
    - `record.created`
- Permite que cada serviço crie filas e bindings específicos para o que quer ouvir.

### 2.2 Filas e bindings (visão geral)

Cada instância de serviço possui **as suas próprias filas**, com um nome que reflete:

- O tipo de evento.
- O serviço.
- Opcionalmente, o identificador da instância.

Exemplos:

- `patient-service.instance-1.events`
- `physician-service.instance-2.events`
- `scheduling-service.instance-1.saga`
- `clinical-records.instance-1.events`

Bindings típicos:

- `hap.patients`:
    - `routing-key = patient.*` ligado às filas de serviços interessados (por ex. Scheduling, Records, Reporting).
- `hap.consultations`:
    - `routing-key = consultation.*` ligado a Scheduling (para sagas internas), Records (para cancelamentos), Reporting, etc.
- `hap.records`:
    - `routing-key = record.*` ligado a serviços de reporting/analytics e export.

---

## 3. Estrutura das mensagens e convenções

### 3.1 Payload

Todas as mensagens usam **JSON** no corpo, com a seguinte estrutura base:

```json
{
  "eventId": "uuid",
  "eventType": "ConsultationScheduled",
  "occurredAt": "2025-10-15T14:23:00Z",
  "sourceService": "scheduling-service",
  "payload": {
    "...": "campos específicos do evento"
  }
}
```


### 3.2 Headers AMQP (metadados)

*   x-correlation-id — correlaciona eventos com a chamada original.

*   x-saga-id — identifica instância da SAGA.

*   x-retry-count — opcional, útil para testes/debug.

### 3.3 Semântica de entrega

- At-least-once delivery 
Consumidores devem ser idempotentes, porque podem receber a mesma mensagem duas vezes.

Técnicas de idempotência:

- Guardar eventId em tabela de eventos processados.

- Usar UPSERT / ON CONFLICT DO NOTHING nos read models.


## 4. Eventos de domínio por serviço

### 4.1 Patient Service

**Publica:**
- PatientRegistered
- PatientUpdated
- PatientDeactivated


### 4.2 Physician Service

**Publica:**
- PhysicianRegistered
- PhysicianUpdated
- PhysicianDeactivated


### 4.3 Scheduling Service (Consultations)

**Publica:**
- ConsultationSchedulingStarted
- ConsultationScheduled
- ConsultationUpdated
- ConsultationCancellationRequested
- ConsultationCancelled
- ConsultationDone

**Consome:**

Para SAGA de agendamento:
- PatientValidatedForConsultation
- PatientValidationFailed
- PhysicianAvailabilityConfirmed
- PhysicianAvailabilityRejected

Para SAGA de cancelamento:
- ConsultationRecordsVoided

### 4.4 Clinical Records Service

**Publica:**
- ConsultationRecordCreated
- ConsultationRecordUpdated
- ConsultationRecordsVoided

**Consome:**
- ConsultationCancellationRequested
- Participa na SAGA de cancelamento.

## 5. Papel do broker nas Sagas

### 5.1 SAGA 1 — Agendar Consulta

Fluxo via eventos:

1. **Scheduling** publica ConsultationSchedulingStarted

2. **Patient Service** valida paciente > publica PatientValidatedForConsultation

3. **Physician Service** valida médico > publica PhysicianAvailabilityConfirmed

4. **Scheduling** recebe ambos > confirma consulta > ConsultationScheduled

Se falhar:

- Publica ConsultationCancelled ou evento de falha equivalente.

### 5.2 SAGA 2 — Cancelar Consulta + Invalidar Registos

1. **Scheduling** publica ConsultationCancellationRequested

2. **Clinical Records** recebe > marca records como VOIDED > publica ConsultationRecordsVoided

3. **Scheduling** recebe > finaliza cancelamento > ConsultationCancelled

## 6. Integração com CQRS e read models

### 6.1 Atualização de read models por evento

Cada instância consome eventos e atualiza o seu próprio read DB:

- Instâncias de Scheduling lêem:
    - ConsultationScheduled
    - ConsultationUpdated
    - ConsultationCancelled

- Instâncias de Clinical Records lêem:
  - ConsultationRecordCreated
  - ConsultationRecordUpdated
  - ConsultationRecordsVoided

Isto cria **database-per-instance** do lado do read model.

### 6.2 Consistência eventual

- Há delay entre o write model e cada read model local.

- Queries podem devolver dados ligeiramente desatualizados.

- Isto é esperado e aceite no modelo CQRS + eventos.

## 7. Fiabilidade, erros e dead-letter queues

## 7.1 Estratégia de entrega

- ACK manual após consumo bem-sucedido.

- NACK + requeue em falha recuperável.

- NACK + DLQ em falha permanente (mensagem inválida).

## 7.2 Dead-Letter Queues (DLQ)

Para cada fila existe uma DLQ:

- scheduling-service.instance-1.events.dlq

- clinical-records.instance-2.events.dlq

Uso:

- Inspeção manual de mensagens problemáticas.

- Evitar avalanche de erros ou bloqueios.

## 8. Testes e validação

### 8.1 Testar publicação de eventos

- Chamar comando > verificar que evento aparece na fila.

### 8.2 Testar consumo e atualização de read models

- Criar consulta > confirmar que o read model da instância #1 foi atualizado.

### 8.3 Testar Sagas

- Agendamento com sucesso.

- Agendamento com falha (physician condenado / paciente inativo).

- Cancelamento de consulta com records > check do evento ConsultationRecordsVoided.

### 8.4 Testar idempotência

- Entregar o mesmo evento 2x > estado final deve ser o mesmo.

## 9. Resumo

- O broker AMQP é central na Parte 2 da arquitetura:

    - Propaga eventos entre domínios.

    - Alinha com CQRS (read models por instância).

    - Permite Sagas distribuídas.

    - Suporta falhas parciais e escalabilidade horizontal.

Este documento liga-se diretamente a:

- 03-cqrs-design.md (comandos, queries, read models)

- 05-deployment-multi-instance.md (como levantar várias instâncias + broker)

- 06-ddd-and-sagas.md (detalhes das Sagas com diagramas)