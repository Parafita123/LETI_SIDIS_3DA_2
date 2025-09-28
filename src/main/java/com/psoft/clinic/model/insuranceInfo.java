package com.psoft.clinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor
public class insuranceInfo {

    @Getter
    @Setter
    @Column(nullable = false,unique = false)
    private String insuranceprovider;
    @Getter
    @Setter
    @Column(nullable = false,unique = false)
    private String policyNumber;
}
