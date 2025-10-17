package com.psoft2024._5.grupo1.projeto_psoft.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;


@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Campos originais (mantidos)
    @NotBlank
    private String name;

    @NotBlank
    private String birthDate;

    private String insuranceProvider;

    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    private String photoUrl;

    @ElementCollection
    @CollectionTable(
            name = "patient_health_concerns",
            joinColumns = @JoinColumn(name = "patient_id")
    )
    @Column(name = "concern")

    private List<String> healthConcerns = new ArrayList<>();

    // Campos adicionais para WP2A
    @NotBlank
    private String fullName;

    @Email @NotBlank
    @Column(unique = true)
    private String email;

    @NotNull
    private LocalDate birthDateLocal;

    @NotBlank
    private String phoneNumber;

    private String insurancePolicyNumber;
    private String insuranceCompany;

    @NotNull
    private LocalDate consentDate;

    // Construtores
    public Patient() {}

    public Patient(String name,
                   String insuranceProvider,
                   User user,
                   String birthDate) {
        this.name = name;
        this.insuranceProvider = insuranceProvider;
        this.user = user;
        this.birthDate = birthDate;
    }

    // Getters & Setters para todos os campos

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getInsuranceProvider() { return insuranceProvider; }
    public void setInsuranceProvider(String insuranceProvider) { this.insuranceProvider = insuranceProvider; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getBirthDateLocal() { return birthDateLocal; }
    public void setBirthDateLocal(LocalDate birthDateLocal) { this.birthDateLocal = birthDateLocal; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getInsurancePolicyNumber() { return insurancePolicyNumber; }
    public void setInsurancePolicyNumber(String insurancePolicyNumber) { this.insurancePolicyNumber = insurancePolicyNumber; }

    public String getInsuranceCompany() { return insuranceCompany; }
    public void setInsuranceCompany(String insuranceCompany) { this.insuranceCompany = insuranceCompany; }

    public LocalDate getConsentDate() { return consentDate; }
    public void setConsentDate(LocalDate consentDate) { this.consentDate = consentDate; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public List<String> getHealthConcerns() { return healthConcerns; }
    public void setHealthConcerns(List<String> healthConcerns) {
        this.healthConcerns = healthConcerns == null ? new ArrayList<>() : healthConcerns;
    }
}
