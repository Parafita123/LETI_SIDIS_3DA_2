package com.psoft.clinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Embeddable
@NoArgsConstructor
public class dataConsent {

    @Getter
    @Setter
    @Column(nullable = false,unique = false)
    private String consent;

    @Getter
    @Setter
    @CreatedDate
    @Column(name = "dateOfConsent", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void prePersist() {
        this.createdAt = Instant.now();
    }
}
