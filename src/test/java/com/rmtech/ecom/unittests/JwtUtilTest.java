package com.rmtech.ecom.unittests;

import com.rmtech.ecom.Config.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(
                jwtUtil,
                "secret",
                "testsecretkeytestsecretkeytestsecretkey12345"
        );
        jwtUtil.init();
    }

    @Test
    void shouldGenerateTokenAndExtractUsername() {
        String token = jwtUtil.generateToken("razik");

        assertEquals("razik", jwtUtil.getUsernameFromToken(token));
    }

    @Test
    void shouldValidateTokenOnlyForMatchingUsername() {
        String token = jwtUtil.generateToken("razik");

        assertTrue(jwtUtil.validateToken(token, "razik"));
        assertFalse(jwtUtil.validateToken(token, "other"));
    }

    @Test
    void shouldReturnFalseForMalformedToken() {
        assertFalse(jwtUtil.validateToken("bad-token", "razik"));
    }

    @Test
    void shouldExtractFutureExpiration() {
        String token = jwtUtil.generateToken("razik");

        LocalDateTime expiration = jwtUtil.extractExpiration(token);

        assertTrue(expiration.isAfter(LocalDateTime.now()));
    }
}
