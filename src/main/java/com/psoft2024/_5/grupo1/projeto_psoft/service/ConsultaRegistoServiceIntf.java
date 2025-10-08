package com.psoft2024._5.grupo1.projeto_psoft.service;

import com.psoft2024._5.grupo1.projeto_psoft.dto.*;
import java.util.List;

public interface ConsultaRegistoServiceIntf {

    ConsultasRegistoOutPutDTO criarRegisto(ConsultasRegistoDTO dto);
    List<ConsultasRegistoOutPutDTO> getRegistosByConsultaId(Long consultaId);
    ConsultasRegistoOutPutDTO getById(Long id);
    ConsultasRegistoOutPutDTO updateRegisto(Long id, ConsultasRegistoDTO dto);
    String generatePrescription(Long id);
}
