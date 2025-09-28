package com.psoft.clinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor
public class Address {

    @Getter
    @Setter
    @Column(unique = false,nullable = false)
    private String street;
    @Getter
    @Setter
    @Column(unique = false,nullable = false)
    private String city;
    @Getter
    @Setter
    @Column(unique = false,nullable = false)
    private String district;
    @Getter
    @Setter
    @Column(unique = false,nullable = false)
    private String zip;
    @Getter
    @Setter
    @Column(unique = false,nullable = false)
    private String country;

}
