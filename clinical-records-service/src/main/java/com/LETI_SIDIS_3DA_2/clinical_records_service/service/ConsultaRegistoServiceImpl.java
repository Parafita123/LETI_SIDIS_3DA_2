package com.LETI_SIDIS_3DA_2.clinical_records_service.service;

// Entidades do próprio serviço
import com.LETI_SIDIS_3DA_2.clinical_records_service.domain.ConsultaRegisto;

// DTOs do próprio serviço
import com.LETI_SIDIS_3DA_2.clinical_records_service.dto.CreateConsultaRegistoDTO;
import com.LETI_SIDIS_3DA_2.clinical_records_service.dto.ConsultaRegistoOutputDTO;

// Exceções customizadas
import com.LETI_SIDIS_3DA_2.clinical_records_service.exception.DuplicateResourceException;
import com.LETI_SIDIS_3DA_2.clinical_records_service.exception.ResourceNotFoundException; // Para o futuro

// Repositórios
import com.LETI_SIDIS_3DA_2.clinical_records_service.repository.ConsultaRegistoRepository;

// Anotações Spring
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Utilitários Java
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ConsultaRegistoServiceImpl implements ConsultaRegistoService {
    private final ConsultaRegistoRepository recordRepository;

    @Autowired
    public ConsultaRegistoServiceImpl(ConsultaRegistoRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @Override
    @Transactional
    public ConsultaRegistoOutputDTO createRecord(CreateConsultaRegistoDTO dto) {
        // TODO: Validação. Antes de criar, deveríamos verificar se a consulta (appointment) com 'dto.getConsultaId()'
        // existe e está "COMPLETED". Isto exigiria uma chamada REST para o Scheduling Service.
        // Por agora, vamos assumir que os dados são válidos.

        // Verifica se já existe um registo para esta consulta
        if (recordRepository.findByConsultaId(dto.getConsultaId()).isPresent()) {
            throw new DuplicateResourceException("Já existe um registo para a consulta com ID: " + dto.getConsultaId());
        }

        ConsultaRegisto record = new ConsultaRegisto(
                dto.getConsultaId(),
                dto.getDiagnosis(),
                dto.getTreatmentRecommendations(),
                dto.getPrescriptions(),
                LocalDateTime.now()
        );

        ConsultaRegisto savedRecord = recordRepository.save(record);
        return convertToDTO(savedRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConsultaRegistoOutputDTO> getRecordByConsultaId(Long consultaId) {
        return recordRepository.findByConsultaId(consultaId)
                .map(this::convertToDTO);
    }

    private ConsultaRegistoOutputDTO convertToDTO(ConsultaRegisto record) {
        if (record == null) {
            return null;
        }

        // Declara a variável dto
        ConsultaRegistoOutputDTO dto = new ConsultaRegistoOutputDTO();

        // Agora usa os setters que acabaste de criar no DTO de saída
        dto.setId(record.getId());
        dto.setConsultaId(record.getConsultaId());
        dto.setDiagnosis(record.getDiagnosis());
        dto.setTreatmentRecommendations(record.getTreatmentRecommendations());
        dto.setPrescriptions(record.getPrescriptions());
        dto.setCreatedAt(record.getCreatedAt());

        return dto; // Retorna o objeto DTO preenchido
    }
}