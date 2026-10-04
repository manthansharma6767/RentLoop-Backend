package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.UpdateProfileRequest;
import com.manthan.rentloop.dto.UserProfileDto;
import com.manthan.rentloop.model.Role;
import com.manthan.rentloop.model.User;
import com.manthan.rentloop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPhone("9876543210");
        user.setRole(Role.USER);
        user.setCreatedAt(Instant.now());
    }

    @Test
    void getUserProfile_Success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        UserProfileDto profile = userService.getUserProfile("john@example.com");

        assertNotNull(profile);
        assertEquals("John Doe", profile.getName());
        assertEquals("john@example.com", profile.getEmail());
        assertEquals("9876543210", profile.getPhone());
        assertEquals("USER", profile.getRole());
    }

    @Test
    void getUserProfile_NotFound_ThrowsException() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.getUserProfile("nonexistent@example.com")
        );

        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void updateProfile_Success() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("John Updated");
        request.setPhone("1112223333");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        UserProfileDto updated = userService.updateProfile("john@example.com", request);

        assertNotNull(updated);
        assertEquals("John Updated", updated.getName());
        assertEquals("1112223333", updated.getPhone());
        verify(userRepository, times(1)).save(user);
    }
}
