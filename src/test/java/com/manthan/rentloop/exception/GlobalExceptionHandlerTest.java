package com.manthan.rentloop.exception;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleBadCredentials_Returns401() {
        BadCredentialsException ex = new BadCredentialsException("Invalid password");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleBadCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password", response.getBody().get("error"));
    }

    @Test
    void handleAccessDenied_Returns403() {
        AccessDeniedException ex = new AccessDeniedException("Access denied: unauthorized operation");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied: unauthorized operation", response.getBody().get("error"));
    }

    @Test
    void handleIllegalArgument_Returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid date range");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid date range", response.getBody().get("error"));
    }

    @Test
    void handleDataIntegrityViolation_Returns409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Duplicate key");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Database constraint violation occurred", response.getBody().get("error"));
    }

    @Test
    void handleJwtExceptions_Returns401() {
        JwtException ex = new JwtException("Expired token");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleJwtExceptions(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication failed: invalid or expired token", response.getBody().get("error"));
    }

    @Test
    void handleGenericException_Returns500WithoutInternalStacktrace() {
        Exception ex = new RuntimeException("Null pointer inside internal engine");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected internal server error occurred", response.getBody().get("error"));
    }
}
