package com.psoft.clinic.appointmentsmanagement.services;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PhysicianCount {
    private Long physicianId;
    private String physicianFullName;
    private long appointmentsCount;

    public PhysicianCount(Long physicianId, String physicianFullName, long appointmentsCount) {
        this.physicianId = physicianId;
        this.physicianFullName = physicianFullName;
        this.appointmentsCount = appointmentsCount;
    }

}
