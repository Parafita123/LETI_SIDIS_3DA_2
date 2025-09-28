package com.psoft.clinic.patientmanagement.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.psoft.clinic.model.BaseUser;
import com.psoft.clinic.model.*;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "Patient")
@Getter
@Setter
public class Patient implements UserDetails {

    @Id
    @Pattern(regexp = "^P\\d{4}\\d{5}$",
            message = "Formato inválido: use PYYYYNNNNN")
    private String patientId;

    @Column(name="sns_number",unique = true,nullable = true,length = 9)
    private String nsns;

    @Embedded
    private BaseUser baseUser;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Email
    private String email;

    @Embedded
    private Phone phone;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateOfBirth;

    @Embedded
    @Valid
    private insuranceInfo insuranceInfo;

    @Embedded

    private dataConsent dataConsent;

    private String image;

    @ElementCollection
    @CollectionTable(name = "patient_health_concern",
            joinColumns = @JoinColumn(name = "patient_id"))
    private List<HealthConcern> healthConcerns = new ArrayList<>();


    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = baseUser.getRole();
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return baseUser.getPassword();
    }

    @Override
    public String getUsername() {
        return baseUser.getUsername();
    }

}
