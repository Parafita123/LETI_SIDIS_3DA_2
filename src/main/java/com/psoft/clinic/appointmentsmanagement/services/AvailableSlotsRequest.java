package com.psoft.clinic.appointmentsmanagement.services;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AvailableSlotsRequest {
    private String date;
    private String physicianFullName;
}
