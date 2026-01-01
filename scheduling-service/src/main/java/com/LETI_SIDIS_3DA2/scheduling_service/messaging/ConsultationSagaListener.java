package com.LETI_SIDIS_3DA2.scheduling_service.messaging;

import com.LETI_SIDIS_3DA2.scheduling_service.domain.Consulta;
import com.LETI_SIDIS_3DA2.scheduling_service.events.EventStoreAppender;
import com.LETI_SIDIS_3DA2.scheduling_service.repository.ConsultaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class ConsultationSagaListener {

    private static final Logger log = LoggerFactory.getLogger(ConsultationSagaListener.class);

    private final ConsultaRepository consultaRepo;
    private final RabbitTemplate rabbitTemplate;
    private final ConsultationEventPublisher eventPublisher;
    private final EventStoreAppender eventStore;

    @Value("${hap.messaging.saga.exchange}")
    private String sagaExchange;

    public ConsultationSagaListener(ConsultaRepository consultaRepo,
                                    RabbitTemplate rabbitTemplate,
                                    ConsultationEventPublisher eventPublisher,
                                    EventStoreAppender eventStore) {
        this.consultaRepo = consultaRepo;
        this.rabbitTemplate = rabbitTemplate;
        this.eventPublisher = eventPublisher;
        this.eventStore = eventStore;
    }

    @RabbitListener(queues = "${hap.messaging.scheduling.saga-queue}")
    public void handleSagaEvent(
            DomainEvent<Map<String, Object>> event,
            @Header(name = "x-correlation-id", required = false) String correlationId,
            @Header(name = "x-saga-id", required = false) String sagaId
    ) {
        log.info("scheduling-service SAGA → recebeu evento: type={} sagaId={} corrId={}",
                event.getEventType(), sagaId, correlationId);

        String type = event.getEventType();

        switch (type) {
            case "ConsultationRequested" ->
                    handleConsultationRequested(event, sagaId, correlationId);

            case "PatientValidatedForConsultation" ->
                    handlePatientValidated(event, sagaId, correlationId);

            case "PatientValidationFailed" ->
                    handlePatientValidationFailed(event, sagaId, correlationId);

            case "PhysicianAvailabilityConfirmed" ->
                    handlePhysicianAvailabilityConfirmed(event, sagaId, correlationId);

            case "PhysicianAvailabilityRejected" ->
                    handlePhysicianAvailabilityRejected(event, sagaId, correlationId);

            case "ConsultationCancellationRequested" ->
                    handleConsultationCancellationRequested(event, sagaId, correlationId);

            case "PhysicianCancellationConfirmed" ->
                    handlePhysicianCancellationConfirmed(event, sagaId, correlationId);

            case "PhysicianCancellationRejected" ->
                    handlePhysicianCancellationRejected(event, sagaId, correlationId);

            default ->
                    log.warn("Evento de SAGA desconhecido: {}", type);
        }
    }

    // ------------- 1) Arranque do SAGA: ConsultationRequested -------------

    private void handleConsultationRequested(DomainEvent<Map<String, Object>> event,
                                             String sagaId,
                                             String correlationId) {

        Map<String, Object> payload = event.getPayload();

        Long consultaId = ((Number) payload.get("id")).longValue();
        Long patientId = ((Number) payload.get("patientId")).longValue();
        Long physicianId = ((Number) payload.get("physicianId")).longValue();

        log.info("SAGA[{}] → ConsultationRequested (consultaId={}, patientId={}, physicianId={})",
                sagaId, consultaId, patientId, physicianId);

        // EVENT STORE: regista arranque de saga
        eventStore.append(
                "Consulta",
                consultaId,
                "SagaConsultationRequested",
                "scheduling-service",
                payload,
                sagaId,
                correlationId
        );

        // Comando para validar o paciente
        Map<String, Object> patientCmdPayload = new HashMap<>();
        patientCmdPayload.put("consultaId", consultaId);
        patientCmdPayload.put("patientId", patientId);

        DomainEvent<Map<String, Object>> validatePatient =
                new DomainEvent<>("ValidatePatientForConsultation",
                        "scheduling-service", patientCmdPayload);

        rabbitTemplate.convertAndSend(
                sagaExchange,
                "saga.patient",
                validatePatient,
                message -> copySagaHeaders(message, sagaId, correlationId)
        );

        // Comando para verificar disponibilidade do médico
        Map<String, Object> physicianCmdPayload = new HashMap<>();
        physicianCmdPayload.put("consultaId", consultaId);
        physicianCmdPayload.put("physicianId", physicianId);
        physicianCmdPayload.put("dateTime", payload.get("dateTime"));

        DomainEvent<Map<String, Object>> checkPhysician =
                new DomainEvent<>("CheckPhysicianAvailabilityForConsultation",
                        "scheduling-service", physicianCmdPayload);

        rabbitTemplate.convertAndSend(
                sagaExchange,
                "saga.physician",
                checkPhysician,
                message -> copySagaHeaders(message, sagaId, correlationId)
        );
    }

    private Message copySagaHeaders(Message message, String sagaId, String correlationId) {
        if (sagaId != null) {
            message.getMessageProperties().setHeader("x-saga-id", sagaId);
        }
        if (correlationId != null) {
            message.getMessageProperties().setHeader("x-correlation-id", correlationId);
            message.getMessageProperties().setCorrelationId(correlationId);
        }
        return message;
    }

    // ------------- 2) Resposta do patient-service -------------

    private void handlePatientValidated(DomainEvent<Map<String, Object>> event,
                                        String sagaId,
                                        String correlationId) {
        Map<String, Object> payload = event.getPayload();
        Long consultaId = ((Number) payload.get("consultaId")).longValue();

        Consulta consulta = consultaRepo.findById(consultaId).orElse(null);
        if (consulta == null) {
            log.warn("SAGA[{}] PatientValidatedForConsultation → consulta {} não encontrada",
                    sagaId, consultaId);
            return;
        }

        eventStore.append("Consulta", consultaId, "PatientValidatedForConsultation",
                "scheduling-service", payload, sagaId, correlationId);

        String status = consulta.getStatus();
        if ("PENDING".equals(status)) {
            consulta.setStatus("PATIENT_CONFIRMED");
            consultaRepo.save(consulta);

            eventStore.append("Consulta", consultaId, "ConsultationStatusChanged",
                    "scheduling-service", Map.of("status", "PATIENT_CONFIRMED"), sagaId, correlationId);

            log.info("SAGA[{}] consulta {} → estado=PATIENT_CONFIRMED", sagaId, consultaId);

        } else if ("PHYSICIAN_CONFIRMED".equals(status)) {
            consulta.setStatus("SCHEDULED");
            consultaRepo.save(consulta);

            eventStore.append("Consulta", consultaId, "ConsultationScheduled",
                    "scheduling-service", Map.of("status", "SCHEDULED"), sagaId, correlationId);

            log.info("SAGA[{}] consulta {} → ambos OK → SCHEDULED", sagaId, consultaId);
            publishConsultationScheduled(consulta);
        }
    }

    private void handlePatientValidationFailed(DomainEvent<Map<String, Object>> event,
                                               String sagaId,
                                               String correlationId) {
        Map<String, Object> payload = event.getPayload();
        Long consultaId = ((Number) payload.get("consultaId")).longValue();
        String reason = (String) payload.get("reason");

        Optional<Consulta> opt = consultaRepo.findById(consultaId);
        if (opt.isEmpty()) {
            log.warn("SAGA[{}] PatientValidationFailed → consulta {} não encontrada",
                    sagaId, consultaId);
            return;
        }

        Consulta consulta = opt.get();
        consulta.setStatus("REJECTED");
        consultaRepo.save(consulta);

        eventStore.append("Consulta", consultaId, "ConsultationRejected",
                "scheduling-service", Map.of("reason", reason, "by", "PATIENT"), sagaId, correlationId);

        log.info("SAGA[{}] consulta {} → REJECTED (motivo: {})",
                sagaId, consultaId, reason);

        publishConsultationFailed(consulta, "PATIENT_VALIDATION_FAILED", reason);
    }

    // ------------- 3) Resposta do physician-service -------------

    private void handlePhysicianAvailabilityConfirmed(DomainEvent<Map<String, Object>> event,
                                                      String sagaId,
                                                      String correlationId) {
        Map<String, Object> payload = event.getPayload();
        Long consultaId = ((Number) payload.get("consultaId")).longValue();

        Consulta consulta = consultaRepo.findById(consultaId).orElse(null);
        if (consulta == null) {
            log.warn("SAGA[{}] PhysicianAvailabilityConfirmed → consulta {} não encontrada",
                    sagaId, consultaId);
            return;
        }

        eventStore.append("Consulta", consultaId, "PhysicianAvailabilityConfirmed",
                "scheduling-service", payload, sagaId, correlationId);

        String status = consulta.getStatus();
        if ("PENDING".equals(status)) {
            consulta.setStatus("PHYSICIAN_CONFIRMED");
            consultaRepo.save(consulta);

            eventStore.append("Consulta", consultaId, "ConsultationStatusChanged",
                    "scheduling-service", Map.of("status", "PHYSICIAN_CONFIRMED"), sagaId, correlationId);

            log.info("SAGA[{}] consulta {} → estado=PHYSICIAN_CONFIRMED", sagaId, consultaId);

        } else if ("PATIENT_CONFIRMED".equals(status)) {
            consulta.setStatus("SCHEDULED");
            consultaRepo.save(consulta);

            eventStore.append("Consulta", consultaId, "ConsultationScheduled",
                    "scheduling-service", Map.of("status", "SCHEDULED"), sagaId, correlationId);

            log.info("SAGA[{}] consulta {} → ambos OK → SCHEDULED", sagaId, consultaId);
            publishConsultationScheduled(consulta);
        }
    }

    private void handlePhysicianAvailabilityRejected(DomainEvent<Map<String, Object>> event,
                                                     String sagaId,
                                                     String correlationId) {
        Map<String, Object> payload = event.getPayload();
        Long consultaId = ((Number) payload.get("consultaId")).longValue();
        String reason = (String) payload.get("reason");

        Optional<Consulta> opt = consultaRepo.findById(consultaId);
        if (opt.isEmpty()) {
            log.warn("SAGA[{}] PhysicianAvailabilityRejected → consulta {} não encontrada",
                    sagaId, consultaId);
            return;
        }

        Consulta consulta = opt.get();
        consulta.setStatus("REJECTED");
        consultaRepo.save(consulta);

        eventStore.append("Consulta", consultaId, "ConsultationRejected",
                "scheduling-service", Map.of("reason", reason, "by", "PHYSICIAN"), sagaId, correlationId);

        log.info("SAGA[{}] consulta {} → REJECTED (motivo: {})",
                sagaId, consultaId, reason);

        publishConsultationFailed(consulta, "PHYSICIAN_REJECTED", reason);
    }

    // ------------- 4) SAGA de cancelamento -------------

    private void handleConsultationCancellationRequested(DomainEvent<Map<String, Object>> event,
                                                         String sagaId,
                                                         String correlationId) {
        Map<String, Object> payload = event.getPayload();

        Long consultaId = ((Number) payload.get("id")).longValue();
        Long physicianId = ((Number) payload.get("physicianId")).longValue();

        log.info("SAGA[{}] → ConsultationCancellationRequested (consultaId={}, physicianId={})",
                sagaId, consultaId, physicianId);

        eventStore.append("Consulta", consultaId, "SagaCancellationRequested",
                "scheduling-service", payload, sagaId, correlationId);

        Map<String, Object> cmdPayload = new HashMap<>();
        cmdPayload.put("consultaId", consultaId);
        cmdPayload.put("physicianId", physicianId);
        cmdPayload.put("dateTime", payload.get("dateTime"));

        DomainEvent<Map<String, Object>> cmd =
                new DomainEvent<>("CheckPhysicianCancellationForConsultation",
                        "scheduling-service", cmdPayload);

        rabbitTemplate.convertAndSend(
                sagaExchange,
                "saga.physician",
                cmd,
                message -> copySagaHeaders(message, sagaId, correlationId)
        );
    }

    private void handlePhysicianCancellationConfirmed(DomainEvent<Map<String, Object>> event,
                                                      String sagaId,
                                                      String correlationId) {
        Long consultaId = ((Number) event.getPayload().get("consultaId")).longValue();

        Consulta consulta = consultaRepo.findById(consultaId).orElse(null);
        if (consulta == null) return;

        consulta.setStatus("CANCELLED");
        consultaRepo.save(consulta);

        eventStore.append("Consulta", consultaId, "ConsultationCancelled",
                "scheduling-service", Map.of("status", "CANCELLED"), sagaId, correlationId);

        log.info("SAGA[{}] consulta {} → CANCELLED (physician confirmou)", sagaId, consultaId);

        eventPublisher.publish(
                "hap.consultations",
                "consultation.cancelled",
                "ConsultationCancelled",
                Map.of(
                        "id", consulta.getId(),
                        "patientId", consulta.getPatientId(),
                        "physicianId", consulta.getPhysicianId(),
                        "dateTime", consulta.getDateTime(),
                        "status", consulta.getStatus()
                )
        );
    }

    private void handlePhysicianCancellationRejected(DomainEvent<Map<String, Object>> event,
                                                     String sagaId,
                                                     String correlationId) {
        Map<String, Object> payload = event.getPayload();
        Long consultaId = ((Number) payload.get("consultaId")).longValue();
        String reason = (String) payload.get("reason");

        Consulta consulta = consultaRepo.findById(consultaId).orElse(null);
        if (consulta == null) return;

        // rollback mínimo: volta a SCHEDULED (ou o estado anterior)
        consulta.setStatus("SCHEDULED");
        consultaRepo.save(consulta);

        eventStore.append("Consulta", consultaId, "ConsultationCancellationRejected",
                "scheduling-service", Map.of("reason", reason, "status", "SCHEDULED"), sagaId, correlationId);

        log.info("SAGA[{}] consulta {} → cancellation REJECTED (reason={})", sagaId, consultaId, reason);

        eventPublisher.publish(
                "hap.consultations",
                "consultation.cancellation.failed",
                "ConsultationCancellationFailed",
                Map.of(
                        "id", consulta.getId(),
                        "reason", reason
                )
        );
    }

    // ------------- Helpers domínio -------------

    private void publishConsultationScheduled(Consulta consulta) {
        ConsultationEventPayload payload = new ConsultationEventPayload(
                consulta.getId(),
                consulta.getPatientId(),
                consulta.getPhysicianId(),
                consulta.getDateTime(),
                consulta.getStatus(),
                consulta.getConsultationType()
        );

        eventPublisher.publish(
                "hap.consultations",
                "consultation.scheduled",
                "ConsultationScheduled",
                payload
        );
    }

    private void publishConsultationFailed(Consulta consulta,
                                           String failureCode,
                                           String reason) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", consulta.getId());
        payload.put("patientId", consulta.getPatientId());
        payload.put("physicianId", consulta.getPhysicianId());
        payload.put("dateTime", consulta.getDateTime());
        payload.put("status", consulta.getStatus());
        payload.put("consultationType", consulta.getConsultationType());
        payload.put("failureCode", failureCode);
        payload.put("reason", reason);

        eventPublisher.publish(
                "hap.consultations",
                "consultation.failed",
                "ConsultationSchedulingFailed",
                payload
        );
    }
}
