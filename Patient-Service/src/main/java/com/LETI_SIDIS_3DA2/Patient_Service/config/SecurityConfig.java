package com.LETI_SIDIS_3DA2.Patient_Service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // H2 em dev
                        .requestMatchers("/h2-console/**").permitAll()

                        // ✅ Actuator mínimo
                        .requestMatchers("/actuator/health/**", "/actuator/prometheus").permitAll()

                        // Mantém a vossa decisão gateway-only / open internally
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
