package com.compania.authservice.controller;

import com.compania.authservice.dto.AuthResponse;
import com.compania.authservice.dto.LoginRequest;
import com.compania.authservice.dto.RegisterRequest;
import com.compania.authservice.security.CustomUserDetailsService;
import com.compania.authservice.security.JwtAuthenticationFilter;
import com.compania.authservice.security.JwtService;
import com.compania.authservice.security.SecurityConfig;
import com.compania.authservice.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/auth/register - Registro exitoso retorna 201")
    void register_ValidRequest_Returns201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan@test.com")
                .password("password123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .email("juan@test.com")
                .nombre("Juan")
                .mensaje("Usuario registrado exitosamente")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("juan@test.com"))
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Request inválido retorna 400")
    void register_InvalidRequest_Returns400() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .nombre("")
                .apellido("")
                .email("invalid-email")
                .password("12")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - Login exitoso retorna 200")
    void login_ValidCredentials_Returns200() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("juan@test.com")
                .password("password123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .email("juan@test.com")
                .nombre("Juan")
                .mensaje("Inicio de sesión exitoso")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.mensaje").value("Inicio de sesión exitoso"));
    }

    @Test
    @DisplayName("GET /api/auth/validate - Token válido retorna 200")
    void validateToken_ValidToken_Returns200() throws Exception {
        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractUsername("valid-token")).thenReturn("juan@test.com");

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.email").value("juan@test.com"));
    }

    @Test
    @DisplayName("GET /api/auth/validate - Token inválido retorna 401")
    void validateToken_InvalidToken_Returns401() throws Exception {
        when(jwtService.isTokenValid("invalid-token")).thenReturn(false);

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }
}
