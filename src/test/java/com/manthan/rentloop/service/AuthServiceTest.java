package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.AuthResponse;
import com.manthan.rentloop.dto.LoginRequest;
import com.manthan.rentloop.dto.RegisterRequest;
import com.manthan.rentloop.model.Role;
import com.manthan.rentloop.model.User;
import com.manthan.rentloop.repository.UserRepository;
import com.manthan.rentloop.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setPhone("1234567890");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        user.setRole(Role.USER);
    }

    @Test
    void register_Success_CreatesUserWithHashedPasswordAndDefaultRole() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("fake-jwt-token");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertTrue(response.getMessage().contains("User registered successfully"));

        verify(userRepository, times(1)).save(argThat(u ->
                u.getEmail().equals("test@example.com") &&
                u.getRole() == Role.USER &&
                u.getPasswordHash().equals("hashedPassword")
        ));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authService.register(registerRequest)
        );

        assertTrue(exception.getMessage().contains("Email is already registered"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success_ReturnsJwtToken() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("fake-jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
        verify(userRepository, never()).findByEmail(anyString());
    }
}
