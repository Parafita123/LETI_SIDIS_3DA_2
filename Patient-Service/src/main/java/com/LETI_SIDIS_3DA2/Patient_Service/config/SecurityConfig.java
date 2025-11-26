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
                // Sem sessão; só para evitar surpresas
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // CSRF off (necessário para H2 Console e para chamadas POST via Postman sem cookie CSRF)
                .csrf(csrf -> csrf.disable())

                // Permite o H2 Console (usa frames)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                .authorizeHttpRequests(auth -> auth
                        // CORS preflight (se tiveres UI a chamar)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Endpoints públicos úteis em dev
                        .requestMatchers("/h2-console/**", "/actuator/**").permitAll()

                        // TODO: como estamos em Gateway-only, abrimos tudo internamente
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
