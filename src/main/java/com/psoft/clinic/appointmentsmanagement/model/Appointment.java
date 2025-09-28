package com.psoft.clinic.appointmentsmanagement.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.psoft.clinic.patientmanagement.model.Patient;
import com.psoft.clinic.physiciansmanagement.model.Physician;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @Column(nullable = false, name = "date")
    private LocalDate date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @DateTimeFormat(pattern = "HH:mm")
    @Column(nullable = false, name = "StartTime")
    private LocalTime startTime;

    @Column(nullable = false,name = "EndTime")
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private ConsultationType consultationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = true)
    private Patient patient;

    @Column(length = 100)
    private String PatientFullName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "physician_id", unique = false)
    private Physician physician;

    @Column(length = 100)
    private String PhysicianFullName;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;



    @Column(length = 500, nullable = true)
    private String details;


    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "modified_at",nullable = true,updatable = true)
    private Instant modifiedAt;

    @PrePersist
    protected void prePersist() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifiedAt = Instant.now();
    }

}


