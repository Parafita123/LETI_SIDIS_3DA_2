package com.psoft2024._5.grupo1.projeto_psoft.dto;

public class PhysicianBasicInfoDTO {

    private String fullName;
    private String specialtyName;

    public PhysicianBasicInfoDTO(String fullName, String specialtyName) {
        this.fullName = fullName;
        this.specialtyName = specialtyName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialtyName() {
        return specialtyName;
    }

    public void setSpecialtyName(String specialtyName) {
        this.specialtyName = specialtyName;
    }
}