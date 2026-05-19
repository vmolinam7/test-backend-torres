package com.compania.authservice.service;

import com.compania.authservice.dto.AuthResponse;
import com.compania.authservice.dto.LoginRequest;
import com.compania.authservice.dto.RegisterRequest;
import com.compania.authservice.entity.User;
import com.compania.authservice.repository.UserRepository;
import com.compania.authservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan@test.com")
                .password("password123")
                .build();

        loginRequest = LoginRequest.builder()
                .email("juan@test.com")
                .password("password123")
                .build();

        user = User.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan@test.com")
                .password("$2a$10$encodedpassword")
                .build();
    }

    @Test
    @DisplayName("Registro exitoso de usuario")
    void register_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedpassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token-mock");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwt-token-mock", response.getToken());
        assertEquals("juan@test.com", response.getEmail());
        assertEquals("Juan", response.getNombre());
        assertEquals("Usuario registrado exitosamente", response.getMensaje());

        verify(userRepository).existsByEmail("juan@test.com");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(UserDetails.class));
    }

    @Test
    @DisplayName("Registro falla cuando el email ya existe")
    void register_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail("juan@test.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(registerRequest));

        assertTrue(exception.getMessage().contains("El email ya está registrado"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Login exitoso")
    void login_Success() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token-mock");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token-mock", response.getToken());
        assertEquals("juan@test.com", response.getEmail());
        assertEquals("Inicio de sesión exitoso", response.getMensaje());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("juan@test.com");
    }

    @Test
    @DisplayName("Login falla cuando el usuario no existe")
    void login_UserNotFound_ThrowsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }
}
