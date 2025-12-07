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

    @Value("${hap.messaging.consultations-exchange}")
    private String consultationsExchangeName;

    @Value("${hap.messaging.sagas-exchange}")
    private String sagasExchangeName;

    @Value("${hap.messaging.scheduling.saga-queue}")
    private String schedulingSagaQueueName;

    //  CQRS Read Model
    @Value("${hap.messaging.patients-exchange}")
    private String patientsExchangeName;

    @Value("${hap.messaging.physicians-exchange}")
    private String physiciansExchangeName;

    @Value("${hap.messaging.readmodel.queue}")
    private String readModelQueueName;

    //Exchanges

    @Bean
    public TopicExchange consultationsExchange() {
        return new TopicExchange(consultationsExchangeName, true, false);
    }

    @Bean
    public TopicExchange sagasExchange() {
        return new TopicExchange(sagasExchangeName, true, false);
    }

    @Bean
    public TopicExchange patientsExchange() {
        return new TopicExchange(patientsExchangeName, true, false);
    }

    @Bean
    public TopicExchange physiciansExchange() {
        return new TopicExchange(physiciansExchangeName, true, false);
    }

    //Fila da SAGA

    @Bean
    public Queue schedulingSagaQueue() {
        return QueueBuilder.durable(schedulingSagaQueueName).build();
    }

    @Bean
    public Binding schedulingSagaBinding(Queue schedulingSagaQueue,
                                         TopicExchange sagasExchange) {
        return BindingBuilder
                .bind(schedulingSagaQueue)
                .to(sagasExchange)
                .with("saga.consultation.*");
    }

    //Fila para CQRS Read Model

    @Bean
    public Queue schedulingReadModelQueue() {
        return QueueBuilder.durable(readModelQueueName).build();
    }

    // Ouve todos os eventos patient.*
    @Bean
    public Binding schedulingReadModelPatientsBinding(Queue schedulingReadModelQueue,
                                                      TopicExchange patientsExchange) {
        return BindingBuilder
                .bind(schedulingReadModelQueue)
                .to(patientsExchange)
                .with("patient.*");
    }

    //E todos os events physician.*
    @Bean
    public Binding schedulingReadModelPhysiciansBinding(Queue schedulingReadModelQueue,
                                                        TopicExchange physiciansExchange) {
        return BindingBuilder
                .bind(schedulingReadModelQueue)
                .to(physiciansExchange)
                .with("physician.*");
    }

    // ---------- Converter JSON ----------

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper mapper) {
        return new Jackson2JsonMessageConverter(mapper);
    }

    // RabbitTemplate (publisher + listener)
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
