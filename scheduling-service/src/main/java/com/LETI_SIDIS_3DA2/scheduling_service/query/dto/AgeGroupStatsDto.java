package com.LETI_SIDIS_3DA2.scheduling_service.query.dto;

public class AgeGroupStatsDto {
    private String ageGroup;
    private long count;

    // Construtor é bom para quando constróis o objeto no serviço
    public AgeGroupStatsDto(String ageGroup, long count) {
        this.ageGroup = ageGroup;
        this.count    = count;
    }

    // Getters são essenciais para o Jackson (serializador JSON)
    public String getAgeGroup() { return ageGroup; }
    public long getCount()     { return count; }

    // Setters são opcionais mas uma boa prática
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }
    public void setCount(long count) { this.count = count; }
}