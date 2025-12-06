// Em ...clinicalrecordsservice.dto.client.AppointmentDetailsDTO.java
package com.LETI_SIDIS_3DA_2.clinical_records_service.query.dto;

// Este DTO representa os dados que recebemos do Scheduling Service.
// Os campos devem corresponder aos do ConsultaOutPutDTO do outro serviço.
public class AppointmentDetailsDTO {
    private Long id;
    private String status;
    // Adiciona outros campos se precisares deles

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}