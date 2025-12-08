package com.LETI_SIDIS_3DA_2.physician_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
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

    // Exchange para eventos de physician (CQRS)
    @Bean
    public TopicExchange physiciansExchange() {
        return new TopicExchange(physiciansExchangeName, true, false);
    }

    // Exchange da SAGA (o mesmo nome usado no scheduling-service)
    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(sagaExchangeName, true, false);
    }

    // Fila onde o physician-service vai ouvir comandos da SAGA
    @Bean
    public Queue physicianSagaQueue() {
        return QueueBuilder
                .durable(physicianSagaQueueName)
                .build();
    }

    // Binding da fila de SAGA ao exchange de SAGA
    @Bean
    public Binding physicianSagaBinding(Queue physicianSagaQueue,
                                        TopicExchange sagaExchange) {
        // Usa o mesmo padrão de routing key que o scheduling-service envia
        return BindingBuilder
                .bind(physicianSagaQueue)
                .to(sagaExchange)
                .with("saga.consultation.*");
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
