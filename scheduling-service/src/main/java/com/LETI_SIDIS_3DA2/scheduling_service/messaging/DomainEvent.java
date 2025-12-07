package com.LETI_SIDIS_3DA2.scheduling_service.messaging;

import java.time.Instant;
import java.util.UUID;

public class DomainEvent<T> {
    public String eventId = UUID.randomUUID().toString();
    public String eventType;
    public Instant occurredAt = Instant.now();
    public String sourceService;
    public T payload;

    public DomainEvent(String eventType, String sourceService, T payload) {
        this.eventType = eventType;
        this.sourceService = sourceService;
        this.payload = payload;
    }
    // getters e setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public String getSourceService() { return sourceService; }
    public void setSourceService(String sourceService) { this.sourceService = sourceService; }

    public T getPayload() { return payload; }
    public void setPayload(T payload) { this.payload = payload; }
}

