package com.LETI_SIDIS_3DA_2.clinical_records_service.messaging;

import com.LETI_SIDIS_3DA_2.clinical_records_service.domain.ConsultaRegisto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ClinicalRecordEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String clinicalRecordsExchange;

    public ClinicalRecordEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${hap.messaging.exchanges.clinical-records}") String clinicalRecordsExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.clinicalRecordsExchange = clinicalRecordsExchange;
    }

    public void publishClinicalRecordCreated(ConsultaRegisto record) {
        ClinicalRecordEventPayload payload = new ClinicalRecordEventPayload(
                record.getId(),
                record.getConsultaId(),
                record.getDiagnosis(),
                record.getTreatmentRecommendations(),
                record.getPrescriptions(),
                record.getCreatedAt()
        );

        publish("clinical-record.created", "ClinicalRecordCreated", payload);
    }

    private void publish(String routingKey,
                         String eventType,
                         ClinicalRecordEventPayload payload) {

        DomainEvent<ClinicalRecordEventPayload> event =
                new DomainEvent<>(eventType, Instant.now(), "clinical-records-service", payload);

        rabbitTemplate.convertAndSend(clinicalRecordsExchange, routingKey, event);
    }
}
