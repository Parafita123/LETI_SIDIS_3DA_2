package com.psoft2024._5.grupo1.projeto_psoft.domain;
import jakarta.persistence.*;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 10) //adiciona a sigla
    private String acronym;


    protected Department() {}

    public Department(String name, String acronym) {
        this.name = name;
        this.acronym = acronym;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAcronym() { return acronym; }
}