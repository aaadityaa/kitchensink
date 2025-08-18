
package com.mongodb.kitchensink.service;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();
        // Set private fields via reflection (simulates @Value injection)
        setField(jwtService, "secret", "dGVzdGluZy1qd3Qtc2VjcmV0LXRlc3QxMjM0NTY3ODkwMTIzNDU2"); // base64
        setField(jwtService, "expMs", 3600_000L); // 1h
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    void generate_and_validate_token_roundtrip() {
        String token = jwtService.generateToken("user@example.com", "ROLES_ADMIN");
        assertNotNull(token);
        assertFalse(token.isEmpty());

        String username = jwtService.extractUsername(token);
        assertEquals("user@example.com", username);

        Date exp = jwtService.extractExpiration(token);
        assertNotNull(exp);
        assertTrue(exp.after(new Date()));

        UserDetails principal = User.withUsername("user@example.com").password("x").roles("ADMIN").build();
        assertTrue(jwtService.validateToken(token, principal));
        assertFalse(jwtService.isTokenExpired(token));
    }
}
