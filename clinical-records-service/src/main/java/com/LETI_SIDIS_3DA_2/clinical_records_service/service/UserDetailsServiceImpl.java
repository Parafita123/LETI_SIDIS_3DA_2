package com.LETI_SIDIS_3DA_2.clinical_records_service.service;

import com.LETI_SIDIS_3DA_2.clinical_records_service.domain.User;
import com.LETI_SIDIS_3DA_2.clinical_records_service.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
