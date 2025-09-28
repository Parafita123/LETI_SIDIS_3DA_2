package com.psoft.clinic.speciality.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@NoArgsConstructor
@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Speciality {

    @Id
    @Getter
    @Setter
    private String acronym;

    @Getter
    @Setter
    @Column(unique = true,nullable = false)
    private String name;

    @Getter
    @Setter
    @Column(nullable = true)
    private String description;

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
