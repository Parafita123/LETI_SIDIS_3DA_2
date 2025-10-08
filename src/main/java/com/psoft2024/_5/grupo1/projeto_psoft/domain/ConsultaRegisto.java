package com.psoft2024._5.grupo1.projeto_psoft.domain;
import jakarta.persistence.*;
import java.time.LocalDateTime;



@Entity
@Table(name = "ConsultaRegistos")
public class ConsultaRegisto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // record number

    @ManyToOne(optional = false)
    @JoinColumn(name = "consulta_id")
    private Consulta consulta;

    @Column(nullable = false, length = 500)
    private String diagnosis;

    @Column(length = 1000)
    private String treatmentRecommendations;

    @Column(length = 1000)
    private String prescriptions;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected ConsultaRegisto() {}

    public ConsultaRegisto(Consulta consulta, String diagnosis,
                             String treatmentRecommendations, String prescriptions,
                             LocalDateTime createdAt) {
        this.consulta = consulta;
        this.diagnosis = diagnosis;
        this.treatmentRecommendations = treatmentRecommendations;
        this.prescriptions = prescriptions;
        this.createdAt = createdAt;
    }
    public Long getId() {
        return id;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public String getDiagnostico() {
        return diagnosis;
    }

    public String getTratamento() {
        return treatmentRecommendations;
    }

    public String getPrescricoes() {
        return prescriptions;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setConsulta(Consulta consulta) {
        this.consulta = consulta;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnosis = diagnostico;
    }

    public void setTratamento(String tratamento) {
        this.treatmentRecommendations = tratamento;
    }

    public void setPrescricoes(String prescricoes) {
        this.prescriptions = prescricoes;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
