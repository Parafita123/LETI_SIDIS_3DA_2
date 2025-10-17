package com.psoft2024._5.grupo1.projeto_psoft.domain;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "physicians")
public class Physician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200, unique = true)
    private String fullName;

    // Relação com Specialization (Um médico tem uma especialidade)
    @ManyToOne(fetch = FetchType.EAGER) // Eager pode ser ok se sempre precisares da especialidade
    @JoinColumn(name = "specialization_id", nullable = false)
    private Specialization specialty; // ÚNICA especialidade

    // Relação com Department (Um médico pertence a um departamento)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department; // ÚNICO departamento

    // Contact Information
    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false, length = 20, unique = true)
    private String phoneNumber;

    @Column(nullable = false, length = 255) // Informação (Morada)
    private String address;

    // Working Hours (igual para toda a semana)
    @Column(nullable = false)
    private LocalTime workStartTime; // Ex: 09:00

    @Column(nullable = false)
    private LocalTime workEndTime;   // Ex: 17:00

    @Column(columnDefinition = "TEXT", nullable = true) //para descrições mais longas
    private String optionalDescription;

    @Column(length = 255, nullable = true) //caminho relativo ou nome do ficheiro da imagem
    private String profilePhotoPath;

    //construtor protegido para JPA
    protected Physician() {}


    public Physician(String fullName, Specialization specialty, Department department,
                     String email, String phoneNumber, String address,
                     LocalTime workStartTime, LocalTime workEndTime, String optionalDescription) {
        this.fullName = fullName;
        this.specialty = specialty;
        this.department = department;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.workStartTime = workStartTime;
        this.workEndTime = workEndTime;
        this.optionalDescription = optionalDescription;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public Specialization getSpecialty() { return specialty; }
    public Department getDepartment() { return department; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public LocalTime getWorkStartTime() { return workStartTime; }
    public LocalTime getWorkEndTime() { return workEndTime; }
    public String getOptionalDescription() { return optionalDescription; }
    public String getProfilePhotoPath() { return profilePhotoPath; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setSpecialty(Specialization specialty) { this.specialty = specialty; }
    public void setDepartment(Department department) { this.department = department; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setAddress(String address) { this.address = address; }
    public void setWorkStartTime(LocalTime workStartTime) { this.workStartTime = workStartTime; }
    public void setWorkEndTime(LocalTime workEndTime) { this.workEndTime = workEndTime; }
    public void setOptionalDescription(String optionalDescription) { this.optionalDescription = optionalDescription; }
    public void setProfilePhotoPath(String profilePhotoPath) { this.profilePhotoPath = profilePhotoPath; }
}
