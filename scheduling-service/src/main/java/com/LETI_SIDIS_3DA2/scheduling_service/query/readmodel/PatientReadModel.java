package com.LETI_SIDIS_3DA2.scheduling_service.query.readmodel;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "patient_read_model")
public class PatientReadModel {

    @Id
    private Long id;

    private String fullName;
    private String email;
    private LocalDate birthDateLocal;

    public PatientReadModel() {
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getBirthDateLocal() {
        return birthDateLocal;
    }
    public void setBirthDateLocal(LocalDate birthDateLocal) {
        this.birthDateLocal = birthDateLocal;
    }
}
