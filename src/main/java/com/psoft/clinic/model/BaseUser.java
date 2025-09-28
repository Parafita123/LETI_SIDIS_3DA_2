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
public class BaseUser {

    @Getter
    @Setter
    @Column(name = "Name", nullable = false)
    private String fullName;

    @Getter
    @Setter
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Getter
    @Setter
    @Column(name = "password", nullable = false)
    private String password;

    @Getter
    @Setter
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Getter
    @Setter
    private String role;

    @PrePersist
    protected void prePersist() {
        this.createdAt = Instant.now();
    }
}