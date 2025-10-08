package com.psoft2024._5.grupo1.projeto_psoft.dto;

public class AgeGroupStatsDto {
    private String ageGroup;
    private long count;

    public AgeGroupStatsDto(String ageGroup, long count) {
        this.ageGroup = ageGroup;
        this.count    = count;
    }

    public String getAgeGroup() { return ageGroup; }
    public long getCount()     { return count; }
}
