package com.LETI_SIDIS_3DA_2.physician_service.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptGenerator {
    public static void main(String[] args) {
        String raw = "adminpass";
        String encoded = new BCryptPasswordEncoder().encode(raw);
        System.out.println("bcrypt('" + raw + "') = " + encoded);
    }
}
