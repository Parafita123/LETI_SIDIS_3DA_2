package com.LETI_SIDIS_3DA2.scheduling_service.events;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "event_store",
        indexes = {
                @Index(name = "idx_event_store_agg", columnList = "aggregateType,aggregateId"),
                @Index(name = "idx_event_store_saga", columnList = "sagaId"),
                @Index(name = "idx_event_store_corr", columnList = "correlationId"),
                @Index(name = "idx_event_store_time", columnList = "occurredAt")
        }
)
public class StoredEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tipo do agregado (ex: "Consulta")
    @Column(nullable = false, length = 80)
    private String aggregateType;

    // ID do agregado (ex: consultaId)
    @Column(nullable = false)
    private Long aggregateId;

    // Tipo do evento (ex: "ConsultationCreated", "ConsultationScheduled")
    @Column(nullable = false, length = 120)
    private String eventType;

    @Column(nullable = false)
    private Instant occurredAt;

    // De onde veio (ex: "scheduling-service")
    @Column(nullable = false, length = 120)
    private String sourceService;

    @Column(nullable = false, length = 120)
    private String payload;

    // Para ligar Event Sourcing ao Saga/Tracing
    @Column(length = 80)
    private String sagaId;

    @Column(length = 80)
    private String correlationId;

    // JSON do payload do evento
    @Lob
    @Column(nullable = false)
    private String payloadJson;

    // Construtor JPA
    protected StoredEvent() {}

    public StoredEvent(String aggregateType,
                       Long aggregateId,
                       String eventType,
                       String sourceService,
                       String payload,
                       String sagaId,
                       String correlationId) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.sourceService = sourceService;
        this.payload = payload;
        this.sagaId = sagaId;
        this.correlationId = correlationId;
    }

    // getters
    public Long getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public Long getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getSourceService() { return sourceService; }
    public String getSagaId() { return sagaId; }
    public String getCorrelationId() { return correlationId; }
    public String getPayloadJson() { return payloadJson; }
}
