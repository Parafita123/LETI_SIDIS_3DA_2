package com.LETI_SIDIS_3DA2.scheduling_service.events;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoredEventRepository extends JpaRepository<StoredEvent, Long> {

    List<StoredEvent> findByAggregateTypeAndAggregateIdOrderByOccurredAtAsc(
            String aggregateType,
            Long aggregateId
    );

    List<StoredEvent> findBySagaIdOrderByOccurredAtAsc(String sagaId);
}
