package com.LETI_SIDIS_3DA2.Patient_Service.messaging;

import java.time.Instant;

public class DomainEvent<T> {

    private final String type;
    private final Instant occurredAt;
    private final String source;
    private final T payload;


    public DomainEvent(String type, Instant occurredAt, String source, T payload) {
        this.type = type;
        this.occurredAt = occurredAt;
        this.source = source;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public T getPayload() {
        return payload;
    }
}

