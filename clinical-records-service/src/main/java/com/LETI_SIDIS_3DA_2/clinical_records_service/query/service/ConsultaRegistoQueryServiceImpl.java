package com.LETI_SIDIS_3DA_2.clinical_records_service.query.service;

import com.LETI_SIDIS_3DA_2.clinical_records_service.domain.ConsultaRegisto;
import com.LETI_SIDIS_3DA_2.clinical_records_service.query.dto.ConsultaRegistoOutputDTO;
import com.LETI_SIDIS_3DA_2.clinical_records_service.repository.ConsultaRegistoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ConsultaRegistoQueryServiceImpl implements ConsultaRegistoQueryService {

    private final ConsultaRegistoRepository recordRepository;

    public ConsultaRegistoQueryServiceImpl(ConsultaRegistoRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConsultaRegistoOutputDTO> getRecordByConsultaId(Long consultaId) {
        return recordRepository.findByConsultaId(consultaId)
                .map(this::convertToDTO);
    }

    private ConsultaRegistoOutputDTO convertToDTO(ConsultaRegisto record) {
        if (record == null) return null;

        ConsultaRegistoOutputDTO dto = new ConsultaRegistoOutputDTO();
        dto.setId(record.getId());
        dto.setConsultaId(record.getConsultaId());
        dto.setDiagnosis(record.getDiagnosis());
        dto.setTreatmentRecommendations(record.getTreatmentRecommendations());
        dto.setPrescriptions(record.getPrescriptions());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }
}
