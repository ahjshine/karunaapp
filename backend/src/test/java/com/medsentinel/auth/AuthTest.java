package com.medsentinel.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthTest {

    @Test
    void testPasswordMatches() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // Hash in seed.sql
        String seedHash = "$2y$10$5iOvNK7pwmTprByYlNVZZ.uljMdYNGOWiqRuLeLa8OM55e4gL0sxe";
        assertTrue(encoder.matches("password123", seedHash), "password123 must match seed hash");
    }
}
