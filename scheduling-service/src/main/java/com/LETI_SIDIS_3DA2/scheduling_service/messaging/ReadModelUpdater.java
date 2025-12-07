package com.LETI_SIDIS_3DA2.scheduling_service.messaging;

import com.LETI_SIDIS_3DA2.scheduling_service.messaging.DomainEvent;
import com.LETI_SIDIS_3DA2.scheduling_service.query.readmodel.PatientReadModel;
import com.LETI_SIDIS_3DA2.scheduling_service.query.readmodel.PatientReadModelRepository;
import com.LETI_SIDIS_3DA2.scheduling_service.query.readmodel.PhysicianReadModel;
import com.LETI_SIDIS_3DA2.scheduling_service.query.readmodel.PhysicianReadModelRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
public class ReadModelUpdater {

    private final PatientReadModelRepository patientRepo;
    private final PhysicianReadModelRepository physicianRepo;

    public ReadModelUpdater(PatientReadModelRepository patientRepo,
                            PhysicianReadModelRepository physicianRepo) {
        this.patientRepo = patientRepo;
        this.physicianRepo = physicianRepo;
    }

    // Vai ouvir a queue configurada em hap.messaging.readmodel.queue
    @RabbitListener(queues = "${hap.messaging.readmodel.queue}")
    public void handleEvent(DomainEvent<Map<String, Object>> event) {

        String type = event.getEventType();
        Map<String, Object> payload = event.getPayload();

        switch (type) {
            case "PatientRegistered" -> applyPatientRegistered(payload);
            case "PatientUpdated"    -> applyPatientUpdated(payload);
            case "PhysicianRegistered" -> applyPhysicianRegistered(payload);
            case "PhysicianUpdated"    -> applyPhysicianUpdated(payload);
            default -> System.out.println("Evento CQRS ignorado: " + type);
        }
    }

    // PATIENT

    private void applyPatientRegistered(Map<String, Object> payload) {
        PatientReadModel rm = new PatientReadModel();
        Long id = ((Number) payload.get("id")).longValue();
        rm.setId(id);
        rm.setFullName((String) payload.get("fullName"));
        rm.setEmail((String) payload.get("email"));

        Object birth = payload.get("birthDateLocal");
        if (birth instanceof String s) {
            rm.setBirthDateLocal(LocalDate.parse(s)); // assume ISO-8601
        }

        patientRepo.save(rm);
    }

    private void applyPatientUpdated(Map<String, Object> payload) {
        Long id = ((Number) payload.get("id")).longValue();
        PatientReadModel rm = patientRepo.findById(id).orElseGet(PatientReadModel::new);
        rm.setId(id);
        rm.setFullName((String) payload.get("fullName"));
        rm.setEmail((String) payload.get("email"));

        Object birth = payload.get("birthDateLocal");
        if (birth instanceof String s) {
            rm.setBirthDateLocal(LocalDate.parse(s));
        }

        patientRepo.save(rm);
    }

    // PHYSICIAN

    private void applyPhysicianRegistered(Map<String, Object> payload) {
        PhysicianReadModel rm = new PhysicianReadModel();
        Long id = ((Number) payload.get("id")).longValue();
        rm.setId(id);
        rm.setFullName((String) payload.get("fullName"));
        rm.setSpecialty((String) payload.get("specialty"));
        physicianRepo.save(rm);
    }

    private void applyPhysicianUpdated(Map<String, Object> payload) {
        Long id = ((Number) payload.get("id")).longValue();
        PhysicianReadModel rm = physicianRepo.findById(id).orElseGet(PhysicianReadModel::new);
        rm.setId(id);
        rm.setFullName((String) payload.get("fullName"));
        rm.setSpecialty((String) payload.get("specialty"));
        physicianRepo.save(rm);
    }
}
