package com.eaglebank.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JwtUtilTest {

    @Test
    void generateAndValidateToken_roundtrip() {
        JwtUtil jwt = new JwtUtil("01234567890123456789012345678901", 3600000L);
        String token = jwt.generateToken("user@example.com");
        assertNotNull(token);
        String subject = jwt.validateAndGetSubject(token);
        assertEquals("user@example.com", subject);
    }
}