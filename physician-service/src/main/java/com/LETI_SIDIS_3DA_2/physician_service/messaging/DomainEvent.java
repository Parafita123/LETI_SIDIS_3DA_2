package com.LETI_SIDIS_3DA_2.physician_service.messaging;

public class DomainEvent<T> {

    private String eventType;
    private String source;
    private T payload;

    public DomainEvent() {}

    public DomainEvent(String eventType, String source, T payload) {
        this.eventType = eventType;
        this.source = source;
        this.payload = payload;
    }

    public String getEventType() { return eventType; }
    public String getSource() { return source; }
    public T getPayload() { return payload; }
}