package com.LETI_SIDIS_3DA2.scheduling_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    @Value("${hap.messaging.consultations.exchange}")
    private String consultationsExchangeName;

    @Value("${hap.messaging.saga.exchange}")
    private String sagaExchangeName;

    @Value("${hap.messaging.scheduling.saga-queue}")
    private String schedulingSagaQueueName;

    // CQRS Read Model
    @Value("${hap.messaging.patients.exchange}")
    private String patientsExchangeName;

    @Value("${hap.messaging.physicians.exchange}")
    private String physiciansExchangeName;

    @Value("${hap.messaging.readmodel.queue}")
    private String readModelQueueName;

    // ---------- Exchanges ----------
    @Bean
    public TopicExchange consultationsExchange() {
        return new TopicExchange(consultationsExchangeName, true, false);
    }

    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(sagaExchangeName, true, false);
    }

    @Bean
    public TopicExchange patientsExchange() {
        return new TopicExchange(patientsExchangeName, true, false);
    }

    @Bean
    public TopicExchange physiciansExchange() {
        return new TopicExchange(physiciansExchangeName, true, false);
    }

    // ---------- SAGA Queue (Scheduling Orchestrator) ----------
    @Bean
    public Queue schedulingSagaQueue() {
        return QueueBuilder.durable(schedulingSagaQueueName).build();
    }

    /**
     * Replies vindas dos participantes (patient/physician) para o orchestrator
     * Ex: routingKey = "saga.scheduling"
     */
    @Bean
    public Binding schedulingSagaRepliesBinding(Queue schedulingSagaQueue, TopicExchange sagaExchange) {
        return BindingBuilder
                .bind(schedulingSagaQueue)
                .to(sagaExchange)
                .with("saga.scheduling");
    }

    /**
     * Arranque e outros eventos de saga (se usares "saga.consultation.requested", etc.)
     * Ex: routingKey = "saga.consultation.requested"
     */
    @Bean
    public Binding schedulingSagaStartBinding(Queue schedulingSagaQueue, TopicExchange sagaExchange) {
        return BindingBuilder
                .bind(schedulingSagaQueue)
                .to(sagaExchange)
                .with("saga.consultation.*");
    }

    // ---------- CQRS Read Model Queue ----------
    @Bean
    public Queue schedulingReadModelQueue() {
        return QueueBuilder.durable(readModelQueueName).build();
    }

    @Bean
    public Binding schedulingReadModelPatientsBinding(Queue schedulingReadModelQueue, TopicExchange patientsExchange) {
        return BindingBuilder
                .bind(schedulingReadModelQueue)
                .to(patientsExchange)
                .with("patient.*");
    }

    @Bean
    public Binding schedulingReadModelPhysiciansBinding(Queue schedulingReadModelQueue, TopicExchange physiciansExchange) {
        return BindingBuilder
                .bind(schedulingReadModelQueue)
                .to(physiciansExchange)
                .with("physician.*");
    }

    // ---------- JSON Converter ----------
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper mapper) {
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
