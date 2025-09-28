package com.psoft.clinic.configuration.security;

import com.psoft.clinic.model.Phone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JwtUtils {
    private final JwtEncoder jwtEncoder;

    @Value("${clinic.jwtExpirySeconds}")
    private long jwtExpirySeconds;

    public JwtUtils(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(String subject, String roles, Phone phone) {
        Instant now = Instant.now();

        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                .issuer("clinic.psoft.com")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(jwtExpirySeconds))
                .subject(subject)
                .claim("roles", roles);

        if (phone != null && phone.getNumber() != null && !phone.getNumber().isBlank()) {
            builder
                    .claim("phone_number", phone.getNumber());
        }

        JwtClaimsSet claims = builder.build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

}
