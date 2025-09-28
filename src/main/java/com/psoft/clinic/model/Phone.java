package com.psoft.clinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor
public class Phone {

    @Column(name = "phone_number", length = 20, nullable = false)
    @Getter @Setter
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(name = "phone_type", length = 20, nullable = true)
    @Getter @Setter
    private PhoneType type;

    public Phone(String number, PhoneType type) {
        this.number = number;
        this.type = type;
    }
}

