package com.LETI_SIDIS_3DA_2.physician_service.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "system_users") // "users" pode ser palavra reservada em alguns SQLs
public class User {

    public enum Role { // Define o enum AQUI
        ADMIN,
        PATIENT,
        PHYSICIAN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false) // !! Idealmente, guardar hash da password !!
    private String password;

    @Enumerated(EnumType.STRING) // para guardar "ADMIN", "PATIENT" na BD
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false, length = 100) // Adiciona email
    private String email;

    @Column(nullable = false, length = 20)  // Adiciona phoneNumber
    private String phoneNumber;


    protected User() {}


    public User(String username, String password, String role, String email, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.role = Role.valueOf(role);
        this.email = email;
        this.phoneNumber = phoneNumber;
    }


    public User(String username, String password, Role role, String email, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // Getters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
}
