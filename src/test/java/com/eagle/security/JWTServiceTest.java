package com.eagle.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTServiceTest {

    @InjectMocks
    private JWTService jwtService;

    private final String SECRET = "mySuperSecretTestingKeyThatIsLongEnoughForHS512Algorithm";
    private final long EXPIRATION = 3600000; // 1 hour
    private final String USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        // Inject values using Spring's test utility
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION);
    }

    @Test
    void generateToken_ValidInput_ReturnsToken() {
        // Execute
        String token = jwtService.generateToken(USERNAME);

        // Verify
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3); // Valid JWT has 3 parts
    }

    @Test
    void extractUsername_ValidToken_ReturnsUsername() {
        // Setup
        String token = createValidToken();

        // Execute
        String username = jwtService.extractUsername(token);

        // Verify
        assertEquals(USERNAME, username);
    }

    @Test
    void extractUsername_InvalidToken_ThrowsException() {
        // Setup
        String invalidToken = "invalid.token.signature";

        // Execute & Verify
        assertThrows(MalformedJwtException.class, () -> jwtService.extractUsername(invalidToken));
    }

    @Test
    void extractUsername_ExpiredToken_ThrowsException() {
        // Setup
        String expiredToken = createExpiredToken();

        // Execute & Verify
        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(expiredToken));
    }

    @Test
    void isTokenValid_ValidTokenAndUser_ReturnsTrue() {
        // Setup
        String token = createValidToken();
        UserDetails userDetails = User.withUsername(USERNAME)
                .password("password")
                .authorities("ROLE_USER")
                .build();

        // Execute & Verify
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_ValidTokenWrongUser_ReturnsFalse() {
        // Setup
        String token = createValidToken();
        UserDetails wrongUser = User.withUsername("wronguser")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        // Execute & Verify
        assertFalse(jwtService.isTokenValid(token, wrongUser));
    }

    @Test
    void isTokenValid_ExpiredToken_ReturnsFalse() {
        // Setup
        String expiredToken = createExpiredToken();
        UserDetails userDetails = User.withUsername(USERNAME)
                .password("password")
                .authorities("ROLE_USER")
                .build();

        // Execute & Verify
        assertFalse(jwtService.isTokenValid(expiredToken, userDetails));
    }

    @Test
    void isTokenValid_InvalidSignature_ReturnsFalse() {
        // Setup
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTY1MTIzNDU2NywiZXhwIjoxNjUxMjM4MTY3fQ." +
                "invalid_signature";

        UserDetails userDetails = User.withUsername(USERNAME)
                .password("password")
                .authorities("ROLE_USER")
                .build();

        // Execute & Verify
        assertFalse(jwtService.isTokenValid(invalidToken, userDetails));
    }

    @Test
    void extractExpiration_ValidToken_ReturnsDate() {
        // Setup
        String token = createValidToken();
        Instant expectedExpiration = Instant.now().plusMillis(EXPIRATION);

        // Execute
        Date expiration = jwtService.extractExpiration(token);

        // Verify
        assertNotNull(expiration);
        // Allow 1 second variance for test execution time
        assertTrue(Math.abs(expectedExpiration.toEpochMilli() - expiration.getTime()) < 1000);
    }

    @Test
    void isTokenExpired_ValidToken_ReturnsFalse() {
        // Setup
        String token = createValidToken();

        // Execute & Verify
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void isTokenExpired_ExpiredToken_ReturnsTrue() {
        // Setup
        String token = createExpiredToken();

        // Execute & Verify
        assertTrue(jwtService.isTokenExpired(token));
    }

    @Test
    void getSigningKey_ConsistentKey() {
        // Execute
        SecretKey key1 = jwtService.getSigningKey();
        SecretKey key2 = jwtService.getSigningKey();

        // Verify
        assertEquals(key1, key2);
    }

    // Helper methods
    private String createValidToken() {
        return Jwts.builder()
                .subject(USERNAME)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(EXPIRATION)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();
    }

    private String createExpiredToken() {
        return Jwts.builder()
                .subject(USERNAME)
                .issuedAt(Date.from(Instant.now().minus(2, ChronoUnit.HOURS)))
                .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();
    }
}