package com.LETI_SIDIS_3DA2.Patient_Service.messaging;

import com.LETI_SIDIS_3DA2.Patient_Service.domain.Patient;
import com.LETI_SIDIS_3DA2.Patient_Service.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PatientConsultationSagaListener {

    private static final Logger log = LoggerFactory.getLogger(PatientConsultationSagaListener.class);

    private final PatientRepository patientRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${hap.messaging.saga.exchange}")
    private String sagaExchange;

    public PatientConsultationSagaListener(PatientRepository patientRepository,
                                           RabbitTemplate rabbitTemplate) {
        this.patientRepository = patientRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "${hap.messaging.patient.saga-queue}")
    public void handleSagaCommand(
            DomainEvent<Map<String, Object>> event,
            @Header(name = "x-saga-id", required = false) String sagaId,
            @Header(name = "x-correlation-id", required = false) String correlationId
    ) {
        log.info("patient-service → recebeu evento SAGA: type={} sagaId={} corrId={}",
                event.getEventType(), sagaId, correlationId);

        if (!"ValidatePatientForConsultation".equals(event.getEventType())) {
            log.info("Evento SAGA ignorado pelo patient-service: {}", event.getEventType());
            return;
        }

        Map<String, Object> payload = event.getPayload();
        Long patientId = ((Number) payload.get("patientId")).longValue();
        Long consultaId = payload.get("consultaId") != null
                ? ((Number) payload.get("consultaId")).longValue()
                : null;

        Map<String, Object> replyPayload = new HashMap<>();
        replyPayload.put("patientId", patientId);
        replyPayload.put("consultaId", consultaId);

        String replyType;

        Patient patient = patientRepository.findById(patientId).orElse(null);

        if (patient == null) {
            replyType = "PatientValidationFailed";
            replyPayload.put("valid", false);
            replyPayload.put("reason", "Patient not found");
            log.info("PatientValidationFailed: patientId={} não encontrado", patientId);
        } else {
            // Aqui podias meter mais validações (consentDate, etc.)
            replyType = "PatientValidatedForConsultation";
            replyPayload.put("valid", true);
            replyPayload.put("fullName", patient.getFullName());
            replyPayload.put("email", patient.getEmail());
            log.info("PatientValidatedForConsultation: patientId={} OK", patientId);
        }

        DomainEvent<Map<String, Object>> replyEvent =
                new DomainEvent<>(replyType, "patient-service", replyPayload);

        rabbitTemplate.convertAndSend(
                sagaExchange,
                "saga.scheduling",   // routing key que o scheduling-service está a escutar
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
