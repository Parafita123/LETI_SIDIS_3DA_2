package com.LETI_SIDIS_3DA_2.physician_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${hap.messaging.exchanges.physicians}")
    private String physiciansExchangeName;

    @Value("${hap.messaging.saga.exchange}")
    private String sagaExchangeName;

    @Value("${hap.messaging.physician.saga-queue}")
    private String physicianSagaQueueName;

    // Exchange de domínio (eventos physician.*)
    @Bean
    public TopicExchange physiciansExchange() {
        return new TopicExchange(physiciansExchangeName, true, false);
    }

    // Exchange da SAGA (hap.saga)
    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(sagaExchangeName, true, false);
    }

    // Fila onde o physician-service ouve comandos SAGA
    @Bean
    public Queue physicianSagaQueue() {
        return QueueBuilder.durable(physicianSagaQueueName).build();
    }

    // ✅ Binding correto: scheduling envia comandos com routingKey "saga.physician"
    @Bean
    public Binding physicianSagaBinding(Queue physicianSagaQueue, TopicExchange sagaExchange) {
        return BindingBuilder
                .bind(physicianSagaQueue)
                .to(sagaExchange)
                .with("saga.physician");
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
