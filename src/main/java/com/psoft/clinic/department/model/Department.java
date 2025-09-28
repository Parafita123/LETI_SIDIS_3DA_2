package com.psoft.clinic.department.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@NoArgsConstructor
@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Department {

    @Id
    @Getter
    @Setter
    @Length(max = 5)
    private String sigla;

    @Getter
    @Setter
    @Column(unique = true,nullable = false)
    private String nome;

    @Getter
    @Setter
    @Length(max = 100)
    @Column(nullable = true)
    private String descricao;

    @Getter
    @Setter
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void prePersist() {
        this.createdAt = Instant.now();
    }
}
