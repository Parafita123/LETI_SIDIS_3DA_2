package com.psoft.clinic.configuration.security;

import com.psoft.clinic.adminmanagement.repository.SpringBootAdminRepository;

import com.psoft.clinic.patientmanagement.repository.SpringBootPatientRepository;

import com.psoft.clinic.physiciansmanagement.repository.SpringBootPhysicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final SpringBootAdminRepository adminRepo;
    private final SpringBootPatientRepository patientRepo;
    private final SpringBootPhysicianRepository physicianRepo;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return adminRepo.findByBaseUserUsername(username)
                .map(a -> build(a.getBaseUser().getUsername(), a.getBaseUser().getPassword(), a.getBaseUser().getRole()))
                .or(() -> patientRepo.findByBaseUserUsername(username)
                        .map(p -> build(p.getBaseUser().getUsername(), p.getBaseUser().getPassword(), p.getBaseUser().getRole())))
                .or(() -> physicianRepo.findByBaseUserUsername(username)
                        .map(p -> build(p.getBaseUser().getUsername(), p.getBaseUser().getPassword(), p.getBaseUser().getRole())))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private UserDetails build(String username, String password, String role) {
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
        return new User(username, password, Collections.singleton(authority));
    }
}