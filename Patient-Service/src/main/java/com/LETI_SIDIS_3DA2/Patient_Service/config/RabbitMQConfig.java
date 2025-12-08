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

    // Exchange de domínio (eventos patient.*)
    @Value("${hap.messaging.exchanges.patients}")
    private String patientsExchangeName;

    // Exchange de SAGA (hap.saga)
    @Value("${hap.messaging.saga.exchange}")
    private String sagaExchangeName;

    // Fila específica deste serviço para comandos SAGA
    @Value("${hap.messaging.patient.saga-queue}")
    private String patientSagaQueueName;

    // ---------- Exchanges ----------

    @Bean
    public TopicExchange patientsExchange() {
        return new TopicExchange(patientsExchangeName, true, false);
    }

    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(sagaExchangeName, true, false);
    }

    // ---------- Fila SAGA deste serviço ----------

    @Bean
    public Queue patientSagaQueue() {
        return QueueBuilder.durable(patientSagaQueueName).build();
    }

    @Bean
    public Binding patientSagaBinding(Queue patientSagaQueue,
                                      TopicExchange sagaExchange) {
        // MUITO IMPORTANTE: routing key tem de bater certo com o que o scheduling-service usa
        // quando envia o comando para validar o paciente:
        // rabbitTemplate.convertAndSend(sagaExchange, "saga.patient", validatePatient, ...)
        return BindingBuilder
                .bind(patientSagaQueue)
                .to(sagaExchange)
                .with("saga.patient");
    }

    // ---------- Converter JSON ----------

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
