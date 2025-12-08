package com.LETI_SIDIS_3DA2.Patient_Service.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;


@Component
public class PatientEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String patientsExchange;

    public PatientEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${hap.messaging.exchanges.patients}") String patientsExchange
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.patientsExchange = patientsExchange;
    }

    public void publish(String routingKey, String eventType, PatientEventPayload payload) {

        DomainEvent<PatientEventPayload> event =
                new DomainEvent<>(eventType, "patient-service", payload);

        rabbitTemplate.convertAndSend(patientsExchange, routingKey, event);
    }
}
