package com.psoft.clinic.physiciansmanagement.model;


import com.psoft.clinic.model.BaseUser;
import com.psoft.clinic.department.model.Department;
import com.psoft.clinic.model.*;
import com.psoft.clinic.speciality.model.Speciality;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter @Setter
public class Physician implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private BaseUser baseUser;

    @ManyToOne(fetch = FetchType.LAZY,
            cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "department_sigla",
            foreignKey = @ForeignKey(name = "fk_physician_department"))
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY,
            cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "speciality",
            foreignKey = @ForeignKey(name = "fk_physician_speciality"))
    private Speciality speciality;

    @Embedded
    private ContactInfo contactInfo;

    @Embedded
    private WorkingHours workingHours;

    private String image;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = baseUser.getRole();
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @LastModifiedDate
    @Column(name = "modified_at",nullable = true,updatable = true)
    private Instant modifiedAt;

    @PreUpdate
    protected void onUpdate() {
        this.modifiedAt = Instant.now();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return "";
    }
}
