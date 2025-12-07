package com.LETI_SIDIS_3DA2.scheduling_service.messaging;

import java.time.LocalDateTime;

public class ConsultationEventPayload {

    private Long id;
    private Long patientId;
    private Long physicianId;
    private LocalDateTime dateTime;
    private String status;
    private String consultationType;

    public ConsultationEventPayload() { }

    public ConsultationEventPayload(Long id, Long patientId, Long physicianId,
                                    LocalDateTime dateTime, String status,
                                    String consultationType) {
        this.id = id;
        this.patientId = patientId;
        this.physicianId = physicianId;
        this.dateTime = dateTime;
        this.status = status;
        this.consultationType = consultationType;
    }

    // getters e setters
}
