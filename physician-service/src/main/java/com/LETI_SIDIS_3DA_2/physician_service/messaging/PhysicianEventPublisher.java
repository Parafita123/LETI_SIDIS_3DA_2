package com.LETI_SIDIS_3DA_2.physician_service.messaging;

import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PhysicianEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String physiciansExchange;

    public PhysicianEventPublisher(RabbitTemplate rabbitTemplate,
                                   @Value("${hap.messaging.exchanges.physicians}") String physiciansExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.physiciansExchange = physiciansExchange;
    }

    public void publish(String routingKey, String eventType, PhysicianEventPayload payload) {

        DomainEvent<PhysicianEventPayload> event =
                new DomainEvent<>(eventType, "physician-service", payload);

        MessagePostProcessor mpp = message -> {
            message.getMessageProperties().setHeader("x-source", "physician-service");
            return message;
        };

        rabbitTemplate.convertAndSend(physiciansExchange, routingKey, event, mpp);
    }

    public void publishPhysicianCreated(PhysicianEventPayload payload) {
        publish("physician.created", "PhysicianRegistered", payload);
    }

    public void publishPhysicianUpdated(PhysicianEventPayload payload) {
        publish("physician.updated", "PhysicianUpdated", payload);
    }
}
