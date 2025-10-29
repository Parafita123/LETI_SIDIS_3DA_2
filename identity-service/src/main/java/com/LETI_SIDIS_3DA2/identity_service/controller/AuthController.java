package com.LETI_SIDIS_3DA2.identity_service.controller;

import com.LETI_SIDIS_3DA2.identity_service.dto.LoginRequest;
import com.LETI_SIDIS_3DA2.identity_service.dto.LoginResponse;
import com.LETI_SIDIS_3DA2.identity_service.security.JwtUtil;
import com.LETI_SIDIS_3DA2.identity_service.service.UserDetailsServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthController(AuthenticationManager authManager,
                          JwtUtil jwtUtil,
                          UserDetailsServiceImpl userDetailsService) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuário ou senha inválidos");
        }

        UserDetails ud = userDetailsService.loadUserByUsername(req.getUsername());
        String token = jwtUtil.generateToken(ud);
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
