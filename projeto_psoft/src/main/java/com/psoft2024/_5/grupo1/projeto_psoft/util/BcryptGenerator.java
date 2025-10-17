package com.psoft2024._5.grupo1.projeto_psoft.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptGenerator {
    public static void main(String[] args) {
        String raw = "adminpass";
        String encoded = new BCryptPasswordEncoder().encode(raw);
        System.out.println("bcrypt('" + raw + "') = " + encoded);
    }
}
