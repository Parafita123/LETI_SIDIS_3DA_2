package com.LETI_SIDIS_3DA2.scheduling_service.messaging;

import com.LETI_SIDIS_3DA2.scheduling_service.command.service.ConsultaCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class ConsultationSagaListener {

    private static final Logger log = LoggerFactory.getLogger(ConsultationSagaListener.class);

    private final ConsultaCommandService commandService;

    public ConsultationSagaListener(ConsultaCommandService commandService) {
        this.commandService = commandService;
    }

    /**
     * Vai receber TODOS os eventos que chegarem à fila
     * scheduling-service.instance-1.saga, com o formato DomainEvent<?>.
     *
     * Mais tarde vamos fazer o switch por eventType e tratar cada caso.
     */
    @RabbitListener(queues = "${hap.messaging.scheduling.saga-queue}")
    public void handleSagaEvent(
            DomainEvent<?> event,
            @Header(name = "x-correlation-id", required = false) String correlationId,
            @Header(name = "x-saga-id", required = false) String sagaId
    ) {
        log.info("Scheduling-SAGA recebeu evento: type={} sagaId={} corrId={}",
                event.getEventType(), sagaId, correlationId);

        switch (event.getEventType()) {
            case "PatientValidatedForConsultation" -> handlePatientValidated(event, sagaId, correlationId);
            case "PatientValidationFailed" -> handlePatientValidationFailed(event, sagaId, correlationId);
            case "PhysicianAvailabilityConfirmed" -> handlePhysicianAvailabilityConfirmed(event, sagaId, correlationId);
            case "PhysicianAvailabilityRejected" -> handlePhysicianAvailabilityRejected(event, sagaId, correlationId);
            case "ConsultationRecordsVoided" -> handleRecordsVoided(event, sagaId, correlationId);
            default -> log.warn("Evento de SAGA desconhecido: {}", event.getEventType());
        }
    }

    // ------- Handlers específicos (por enquanto só logs / TODO) -------

    private void handlePatientValidated(DomainEvent<?> event, String sagaId, String correlationId) {
        log.info("✔ PatientValidatedForConsultation recebido para sagaId={}", sagaId);
        // TODO: marcar no estado interno da SAGA que o paciente foi validado
    }

    private void handlePatientValidationFailed(DomainEvent<?> event, String sagaId, String correlationId) {
        log.info("✖ PatientValidationFailed recebido para sagaId={}", sagaId);
        // TODO: compensar / cancelar tentativa de agendamento
    }

    private void handlePhysicianAvailabilityConfirmed(DomainEvent<?> event, String sagaId, String correlationId) {
        log.info("✔ PhysicianAvailabilityConfirmed recebido para sagaId={}", sagaId);
        // TODO: quando paciente + médico OK -> confirmar consulta (ConsultaCommandService)
    }

    private void handlePhysicianAvailabilityRejected(DomainEvent<?> event, String sagaId, String correlationId) {
        log.info("✖ PhysicianAvailabilityRejected recebido para sagaId={}", sagaId);
        // TODO: compensar / publicar evento de falha de agendamento
    }

    private void handleRecordsVoided(DomainEvent<?> event, String sagaId, String correlationId) {
        log.info("✔ ConsultationRecordsVoided recebido para sagaId={}", sagaId);
        // TODO: marcar consulta como CANCELLED em definitivo, se ainda não estiver
    }
}
