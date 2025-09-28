package com.psoft.clinic.appointmentsmanagement.services;



public record AgeGroupStatsDto(
        String ageGroup,
        long appointmentCount,
        double averageDurationMinutes
) { }
