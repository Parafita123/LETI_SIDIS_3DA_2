package com.LETI_SIDIS_3DA2.scheduling_service.messaging;

import com.LETI_SIDIS_3DA2.scheduling_service.domain.Consulta;
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

    @Value("${hap.messaging.saga.exchange}")
    private String sagaExchange;

    public ConsultationSagaListener(ConsultaRepository consultaRepo,
                                    RabbitTemplate rabbitTemplate,
                                    ConsultationEventPublisher eventPublisher) {
        this.consultaRepo = consultaRepo;
        this.rabbitTemplate = rabbitTemplate;
        this.eventPublisher = eventPublisher;
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

            case "ConsultationRecordsVoided" ->
                    handleRecordsVoided(event, sagaId, correlationId);

            default ->
                    log.warn("Evento de SAGA desconhecido: {}", type);
        }
    }

    // ------------- 1) Arranque do SAGA: ConsultationRequested -------------

    private void handleConsultationRequested(DomainEvent<Map<String, Object>> event,
                                             String sagaId,
                                             String correlationId) {

        Map<String, Object> payload = event.getPayload();

        Long consultaId  = ((Number) payload.get("id")).longValue();
        Long patientId   = ((Number) payload.get("patientId")).longValue();
        Long physicianId = ((Number) payload.get("physicianId")).longValue();

        log.info("SAGA[{}] → ConsultationRequested (consultaId={}, patientId={}, physicianId={})",
                sagaId, consultaId, patientId, physicianId);

        // Comando para validar o paciente
        Map<String, Object> patientCmdPayload = new HashMap<>();
        patientCmdPayload.put("consultaId", consultaId);
        patientCmdPayload.put("patientId", patientId);

        DomainEvent<Map<String, Object>> validatePatient =
                new DomainEvent<>("ValidatePatientForConsultation",
                        "scheduling-service", patientCmdPayload);

        rabbitTemplate.convertAndSend(
                sagaExchange,
                "saga.patient",    // queue do patient-service está ligada a esta routing key
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
                "saga.physician",  // queue do physician-service está ligada a esta routing key
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

            // AMQP standard correlation id
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

        String status = consulta.getStatus();
        if ("PENDING".equals(status)) {
            consulta.setStatus("PATIENT_CONFIRMED");
            consultaRepo.save(consulta);
            log.info("SAGA[{}] consulta {} → estado=PATI ENT_CONFIRMED", sagaId, consultaId);
        } else if ("PHYSICIAN_CONFIRMED".equals(status)) {
            // Já tínhamos o médico OK → agora marcamos como SCHEDULED
            consulta.setStatus("SCHEDULED");
            consultaRepo.save(consulta);
            log.info("SAGA[{}] consulta {} → ambos OK → SCHEDULED", sagaId, consultaId);
            publishConsultationScheduled(consulta);
        }
    }

    private void handlePatientValidationFailed(DomainEvent<Map<String, Object>> event,
                                               String sagaId,
                                               String correlationId) {
        Map<String, Object> payload = event.getPayload();
        Long consultaId = ((Number) payload.get("consultaId")).longValue();
        String reason   = (String) payload.get("reason");

        Optional<Consulta> opt = consultaRepo.findById(consultaId);
        if (opt.isEmpty()) {
            log.warn("SAGA[{}] PatientValidationFailed → consulta {} não encontrada",
                    sagaId, consultaId);
            return;
        }

        Consulta consulta = opt.get();
        consulta.setStatus("REJECTED");
        consultaRepo.save(consulta);

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

        String status = consulta.getStatus();
        if ("PENDING".equals(status)) {
            consulta.setStatus("PHYSICIAN_CONFIRMED");
            consultaRepo.save(consulta);
            log.info("SAGA[{}] consulta {} → estado=PHYSICIAN_CONFIRMED", sagaId, consultaId);
        } else if ("PATIENT_CONFIRMED".equals(status)) {
            consulta.setStatus("SCHEDULED");
            consultaRepo.save(consulta);
            log.info("SAGA[{}] consulta {} → ambos OK → SCHEDULED", sagaId, consultaId);
            publishConsultationScheduled(consulta);
        }
    }

    private void handlePhysicianAvailabilityRejected(DomainEvent<Map<String, Object>> event,
                                                     String sagaId,
                                                     String correlationId) {
        Map<String, Object> payload = event.getPayload();
        Long consultaId = ((Number) payload.get("consultaId")).longValue();
        String reason   = (String) payload.get("reason");

        Optional<Consulta> opt = consultaRepo.findById(consultaId);
        if (opt.isEmpty()) {
            log.warn("SAGA[{}] PhysicianAvailabilityRejected → consulta {} não encontrada",
                    sagaId, consultaId);
            return;
        }

        Consulta consulta = opt.get();
        consulta.setStatus("REJECTED");
        consultaRepo.save(consulta);

        log.info("SAGA[{}] consulta {} → REJECTED (motivo: {})",
                sagaId, consultaId, reason);

        publishConsultationFailed(consulta, "PHYSICIAN_REJECTED", reason);
    }

    // ------------- 4) Evento vindo do clinical-records (no futuro) -------------

    private void handleRecordsVoided(DomainEvent<Map<String, Object>> event,
                                     String sagaId,
                                     String correlationId) {
        // Isto seria mais relevante para o SAGA de cancelamento + clinical records
        log.info("SAGA[{}] ConsultationRecordsVoided recebido (não tratamos ainda o cancelamento via SAGA)", sagaId);
    }

    // ------------- 5) Helpers para publicar eventos finais -------------

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
