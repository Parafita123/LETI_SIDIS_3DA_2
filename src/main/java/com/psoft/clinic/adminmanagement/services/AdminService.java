package com.psoft.clinic.adminmanagement.services;

import java.util.Optional;
import java.util.Set;

import com.psoft.clinic.adminmanagement.model.Admin;
import com.psoft.clinic.exceptions.UsernameAlreadyExistsException;
import com.psoft.clinic.model.BaseUser;
import com.psoft.clinic.model.Role;
import com.psoft.clinic.adminmanagement.repository.SpringBootAdminRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final SpringBootAdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public Admin create(CreateAdminRequest request) {
        if (adminRepository
                .findByBaseUserUsername(request.getUsername())
                .isPresent()
        ) {
            throw new UsernameAlreadyExistsException(request.getUsername());
        }

        BaseUser baseUser = new BaseUser();
        baseUser.setUsername(request.getUsername());
        baseUser.setPassword(passwordEncoder.encode(request.getPassword()));
        baseUser.setFullName(request.getFullName());
        baseUser.setRole(Role.USER_ADMIN);

        Admin admin = new Admin();
        admin.setBaseUser(baseUser);

        return adminRepository.save(admin);
    }

    public Optional<Admin> findByUsername(String username) {
        return adminRepository.findByBaseUserUsername(username);
    }
}
