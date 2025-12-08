package com.LETI_SIDIS_3DA_2.physician_service.messaging;

import com.LETI_SIDIS_3DA_2.physician_service.domain.Physician;
import com.LETI_SIDIS_3DA_2.physician_service.repository.PhysicianRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class PhysicianConsultationSagaListener {

    private static final Logger log = LoggerFactory.getLogger(PhysicianConsultationSagaListener.class);

    private final PhysicianRepository physicianRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${hap.messaging.saga.exchange}")
    private String sagaExchange;

    public PhysicianConsultationSagaListener(PhysicianRepository physicianRepository,
                                             RabbitTemplate rabbitTemplate) {
        this.physicianRepository = physicianRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "${hap.messaging.physician.saga-queue}")
    public void handleSagaCommand(
            DomainEvent<Map<String, Object>> event,
            @Header(name = "x-saga-id", required = false) String sagaId,
            @Header(name = "x-correlation-id", required = false) String correlationId
    ) {
        log.info("physician-service → recebeu evento SAGA: type={} sagaId={} corrId={}",
                event.getEventType(), sagaId, correlationId);

        if (!"CheckPhysicianAvailabilityForConsultation".equals(event.getEventType())) {
            log.info("Evento SAGA ignorado pelo physician-service: {}", event.getEventType());
            return;
        }

        Map<String, Object> payload = event.getPayload();
        Long physicianId = ((Number) payload.get("physicianId")).longValue();
        Long consultaId = payload.get("consultaId") != null
                ? ((Number) payload.get("consultaId")).longValue()
                : null;

        LocalDateTime dateTime = null;
        Object dtRaw = payload.get("dateTime");
        if (dtRaw instanceof String s) {
            dateTime = LocalDateTime.parse(s); // assume ISO-8601
        }

        Map<String, Object> replyPayload = new HashMap<>();
        replyPayload.put("physicianId", physicianId);
        replyPayload.put("consultaId", consultaId);
        replyPayload.put("dateTime", dateTime != null ? dateTime.toString() : null);

        String replyType;

        Physician physician = physicianRepository.findById(physicianId).orElse(null);

        if (physician == null) {
            replyType = "PhysicianAvailabilityRejected";
            replyPayload.put("available", false);
            replyPayload.put("reason", "Physician not found");
            log.info("PhysicianAvailabilityRejected: physicianId={} não encontrado", physicianId);
        } else {
            // 🔴 Aqui podias fazer verificação real de disponibilidade (horário, overlapping, etc.)
            // Para efeitos de padrão SAGA, vamos assumir que está disponível.
            replyType = "PhysicianAvailabilityConfirmed";
            replyPayload.put("available", true);
            replyPayload.put("fullName", physician.getFullName());
            replyPayload.put("specialty", physician.getSpecialty() != null
                    ? physician.getSpecialty().getName()
                    : null);
            log.info("PhysicianAvailabilityConfirmed: physicianId={} aparentemente disponível", physicianId);
        }

        DomainEvent<Map<String, Object>> replyEvent =
                new DomainEvent<>(replyType, "physician-service", replyPayload);

        rabbitTemplate.convertAndSend(
                sagaExchange,
                "saga.scheduling",
                replyEvent,
                message -> {
                    if (sagaId != null) {
                        message.getMessageProperties().setHeader("x-saga-id", sagaId);
                    }
                    if (correlationId != null) {
                        message.getMessageProperties().setHeader("x-correlation-id", correlationId);
                    }
                    return message;
                }
        );
    }
}
