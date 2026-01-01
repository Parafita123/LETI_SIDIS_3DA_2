package com.LETI_SIDIS_3DA2.scheduling_service.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Responsável por persistir eventos de domínio no Event Store.
 * Este é o ponto central do Event Sourcing.
 */
@Component
public class EventStoreAppender {

    private static final Logger log = LoggerFactory.getLogger(EventStoreAppender.class);

    private final StoredEventRepository repository;
    private final ObjectMapper objectMapper;

    public EventStoreAppender(StoredEventRepository repository,
                              ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /**
     * Guarda um evento no Event Store
     */
    public void append(String aggregateType,
                       Long aggregateId,
                       String eventType,
                       String sourceService,
                       Object payload,
                       String sagaId,
                       String correlationId) {

        try {
            String payloadJson = objectMapper.writeValueAsString(payload);

            StoredEvent storedEvent = new StoredEvent(
                    aggregateType,
                    aggregateId,
                    eventType,
                    sourceService,
                    payloadJson,
                    sagaId,
                    correlationId
            );

            repository.save(storedEvent);

            log.debug(
                    "EventStore ← agregado={} id={} evento={} sagaId={} corrId={}",
                    aggregateType,
                    aggregateId,
                    eventType,
                    sagaId,
                    correlationId
            );

        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar payload do evento {} para EventStore", eventType, e);
            throw new RuntimeException("Falha ao persistir evento no Event Store", e);
        }
    }
}
