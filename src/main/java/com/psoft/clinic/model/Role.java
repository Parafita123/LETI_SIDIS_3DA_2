package com.psoft.clinic.model;

import org.springframework.security.core.GrantedAuthority;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Role implements GrantedAuthority {
    public static final String USER_ADMIN = "USER_ADMIN";
    public static final String USER_PATIENT = "USER_PATIENT";
    public static final String USER_PHYSICIAN = "USER_PHYSICIAN";

    private String authority;
}
