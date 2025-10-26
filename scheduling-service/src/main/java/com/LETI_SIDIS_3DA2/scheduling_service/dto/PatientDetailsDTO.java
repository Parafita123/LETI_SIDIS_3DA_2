package com.LETI_SIDIS_3DA2.scheduling_service.dto;

import java.time.LocalDate;

// Este DTO representa os dados que recebemos do Patient Service.
public class PatientDetailsDTO {
    private Long id;
    private String fullName;
    private LocalDate birthDateLocal;
    private UserDTO user; // DTO para o utilizador associado

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDate getBirthDateLocal() { return birthDateLocal; }
    public void setBirthDateLocal(LocalDate birthDateLocal) { this.birthDateLocal = birthDateLocal; }
    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }
}