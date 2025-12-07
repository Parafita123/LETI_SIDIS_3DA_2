package com.LETI_SIDIS_3DA2.scheduling_service.messaging;


import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ConsultationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public ConsultationEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(String exchange, String routingKey, String eventType, Object payload) {

        Map<String, Object> message = new HashMap<>();
        message.put("eventId", UUID.randomUUID().toString());
        message.put("eventType", eventType);
        message.put("occurredAt", Instant.now().toString());
        message.put("sourceService", "scheduling-service");
        message.put("payload", payload);

        rabbitTemplate.convertAndSend(exchange, routingKey, message);

        System.out.println("📤 Evento enviado -> " + eventType + " → " + routingKey);
    }
}
