package com.compania.authservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    private static final String SECRET = "cGVyc29uYWxTZWNyZXRLZXlGb3JKV1RUb2tlblNpZ25pbmdQdXJwb3NlczIwMjZQcnVlYmFUZWNuaWNh";
    private static final long EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);

        userDetails = new User("test@test.com", "password", new ArrayList<>());
    }

    @Test
    @DisplayName("Genera token JWT correctamente")
    void generateToken_Success() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Extrae username del token")
    void extractUsername_Success() {
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);

        assertEquals("test@test.com", username);
    }

    @Test
    @DisplayName("Token válido retorna true")
    void isTokenValid_ValidToken_ReturnsTrue() {
        String token = jwtService.generateToken(userDetails);
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Token con usuario diferente retorna false")
    void isTokenValid_WrongUser_ReturnsFalse() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otherUser = new User("otro@test.com", "password", new ArrayList<>());

        boolean isValid = jwtService.isTokenValid(token, otherUser);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Token expirado retorna false")
    void isTokenValid_ExpiredToken_ReturnsFalse() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        String expiredToken = Jwts.builder()
                .subject("test@test.com")
                .issuedAt(new Date(System.currentTimeMillis() - 200000))
                .expiration(new Date(System.currentTimeMillis() - 100000))
                .signWith(key)
                .compact();

        boolean isValid = jwtService.isTokenValid(expiredToken, userDetails);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validación simple de token válido")
    void isTokenValid_SimpleValidation_ReturnsTrue() {
        String token = jwtService.generateToken(userDetails);
        boolean isValid = jwtService.isTokenValid(token);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Token inválido retorna false en validación simple")
    void isTokenValid_InvalidToken_ReturnsFalse() {
        boolean isValid = jwtService.isTokenValid("invalid-token");

        assertFalse(isValid);
    }
}
