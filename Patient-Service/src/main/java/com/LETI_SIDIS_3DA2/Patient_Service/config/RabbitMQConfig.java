package com.LETI_SIDIS_3DA2.Patient_Service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${hap.messaging.exchanges.patients}")
    private String patientsExchangeName;

    @Value("${hap.messaging.saga.exchange}")
    private String sagaExchangeName;

    @Value("${hap.messaging.patient.saga-queue}")
    private String patientSagaQueueName;

    @Bean
    public TopicExchange patientsExchange() {
        return new TopicExchange(patientsExchangeName, true, false);
    }

    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(sagaExchangeName, true, false);
    }

    @Bean
    public Queue patientSagaQueue() {
        return QueueBuilder.durable(patientSagaQueueName).build();
    }

    // ✅ scheduling envia "saga.patient"
    @Bean
    public Binding patientSagaBinding(Queue patientSagaQueue, TopicExchange sagaExchange) {
        return BindingBuilder
                .bind(patientSagaQueue)
                .to(sagaExchange)
                .with("saga.patient");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
