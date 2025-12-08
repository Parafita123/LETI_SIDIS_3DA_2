package com.LETI_SIDIS_3DA_2.clinical_records_service.messaging;

import java.time.Instant;

public class DomainEvent<T> {

    private String type;
    private Instant occurredAt;
    private String source;
    private T payload;

    // ctor vazio para Jackson
    public DomainEvent() { }

    public DomainEvent(String type, Instant occurredAt, String source, T payload) {
        this.type = type;
        this.occurredAt = occurredAt;
        this.source = source;
        this.payload = payload;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public T getPayload() { return payload; }
    public void setPayload(T payload) { this.payload = payload; }
}