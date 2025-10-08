package com.psoft2024._5.grupo1.projeto_psoft.service;

import com.psoft2024._5.grupo1.projeto_psoft.domain.User;
import com.psoft2024._5.grupo1.projeto_psoft.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repo;

    public UserDetailsServiceImpl(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(u.getUsername())
                .password(u.getPassword())
                // supõe que User.getRole() retorna um enum com nomes ADMIN, PATIENT, etc.
                .authorities(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()))
                .build();
    }
}
