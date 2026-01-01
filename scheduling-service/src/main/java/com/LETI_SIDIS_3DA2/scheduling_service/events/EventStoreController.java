package com.LETI_SIDIS_3DA2.scheduling_service.events;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/eventstore")
public class EventStoreController {

    private final StoredEventRepository repo;

    public EventStoreController(StoredEventRepository repo) {
        this.repo = repo;
    }

    // Ex: GET /api/admin/eventstore/consultas/12
    @GetMapping("/consultas/{consultaId}")
    public List<StoredEvent> getEventsForConsulta(@PathVariable Long consultaId) {
        return repo.findByAggregateTypeAndAggregateIdOrderByOccurredAtAsc("Consulta", consultaId);
    }

    // Ex: GET /api/admin/eventstore/saga/<sagaId>
    @GetMapping("/saga/{sagaId}")
    public List<StoredEvent> getBySagaId(@PathVariable String sagaId) {
        return repo.findBySagaIdOrderByOccurredAtAsc(sagaId);
    }
}
