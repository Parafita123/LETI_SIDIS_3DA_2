package com.psoft2024._5.grupo1.projeto_psoft.service;

import com.psoft2024._5.grupo1.projeto_psoft.domain.*;
import com.psoft2024._5.grupo1.projeto_psoft.dto.*;
import com.psoft2024._5.grupo1.projeto_psoft.exception.*;
import com.psoft2024._5.grupo1.projeto_psoft.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.time.DayOfWeek;

@Service
public class ConsultaRegistoService implements ConsultaRegistoServiceIntf  {
    private final ConsultasRegistoRepository registoRepo;
    private final ConsultasRepository consultaRepo;

    public ConsultaRegistoService(ConsultasRegistoRepository registoRepo, ConsultasRepository consultaRepo) {
        this.registoRepo = registoRepo;
        this.consultaRepo = consultaRepo;
    }

    @Override
    public ConsultasRegistoOutPutDTO  criarRegisto(ConsultasRegistoDTO dto) {
        Consulta consulta = consultaRepo.findById(dto.getConsultaId())
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada"));

        ConsultaRegisto registo = new ConsultaRegisto(
                consulta,
                dto.getDiagnosis(),
                dto.getTreatmentRecommendations(),
                dto.getPrescriptions(),
                LocalDateTime.now()
        );

        return toDto(registoRepo.save(registo));
    }

    @Override
    public List<ConsultasRegistoOutPutDTO> getRegistosByConsultaId(Long consultaId) {
        return registoRepo.findByConsultaId(consultaId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ConsultasRegistoOutPutDTO getById(Long id) {
        ConsultaRegisto registo = registoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registo não encontrado"));
        return toDto(registo);
    }

    @Override
    public ConsultasRegistoOutPutDTO updateRegisto(Long id, ConsultasRegistoDTO dto) {
        // busca o registro
        ConsultaRegisto existing = registoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registo não encontrado"));
        // atualiza campos permitidos
        existing.setDiagnostico(dto.getDiagnosis());
        existing.setTratamento(dto.getTreatmentRecommendations());
        existing.setPrescricoes(dto.getPrescriptions());
        // opcionalmente atualize createdAt? normalmente não
        ConsultaRegisto saved = registoRepo.save(existing);
        return toDto(saved);
    }

    @Override
    public String generatePrescription(Long id) {
        // aqui você pode implementar geração de receita (PDF, texto, etc.)
        // para começar, vamos retornar o campo de prescrições do registro:
        return registoRepo.findById(id)
                .map(ConsultaRegisto::getPrescricoes)
                .orElseThrow(() -> new ResourceNotFoundException("Registo não encontrado"));
    }




    private ConsultasRegistoOutPutDTO toDto(ConsultaRegisto r) {
        return new ConsultasRegistoOutPutDTO(
                r.getId(),
                r.getConsulta().getId(),
                r.getDiagnostico(),
                r.getTratamento(),
                r.getPrescricoes(),
                r.getCreatedAt()
        );
    }




}
