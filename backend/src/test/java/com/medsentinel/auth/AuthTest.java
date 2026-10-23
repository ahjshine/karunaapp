package com.medsentinel.auth;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthTest {

    @Test
    void testPasswordMatches() {
        String seedHash = "$2a$10$5iOvNK7pwmTprByYlNVZZ.uljMdYNGOWiqRuLeLa8OM55e4gL0sxe";
        assertTrue(BCrypt.checkpw("password123", seedHash), "password123 must match seed hash");
    }
}
