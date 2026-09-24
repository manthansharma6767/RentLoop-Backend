package com.manthan.rentloop.service;
import com.manthan.rentloop.dto.AuthResponse;
import com.manthan.rentloop.dto.LoginRequest;
import com.manthan.rentloop.dto.RegisterRequest;
import com.manthan.rentloop.model.Role;
import com.manthan.rentloop.model.User;
import com.manthan.rentloop.repository.UserRepository;
import com.manthan.rentloop.security.CustomUserDetails;
import com.manthan.rentloop.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(Role.USER); // Default role

        userRepository.save(user);

        String jwtToken = jwtUtil.generateToken(new CustomUserDetails(user));
        return new AuthResponse(jwtToken, "User registered successfully");
    }

    public AuthResponse login(LoginRequest request) {
        // Authenticate credentials via Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // If we reach here, credentials are correct
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String jwtToken = jwtUtil.generateToken(new CustomUserDetails(user));

        return new AuthResponse(jwtToken, "Login successful");
    }
}
