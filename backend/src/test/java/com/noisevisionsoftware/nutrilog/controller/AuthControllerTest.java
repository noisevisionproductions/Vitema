package com.noisevisionsoftware.nutrilog.controller;

import com.noisevisionsoftware.nutrilog.dto.response.ErrorResponse;
import com.noisevisionsoftware.nutrilog.security.model.FirebaseUser;
import com.noisevisionsoftware.nutrilog.service.auth.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private FirebaseUser firebaseUser;

    @InjectMocks
    private AuthController authController;

    private static final String VALID_TOKEN = "valid_token";
    private static final String INVALID_TOKEN = "invalid_token";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_HEADER = BEARER_PREFIX + INVALID_TOKEN;

    @Test
    void login_WithValidToken_ShouldReturnOkWithUser() {
        String validAuthHeader = BEARER_PREFIX + VALID_TOKEN;
        when(firebaseUser.getUid()).thenReturn("test-uid");
        when(firebaseUser.getEmail()).thenReturn("test@example.com");
        when(authService.authenticateAdmin(VALID_TOKEN)).thenReturn(firebaseUser);

        ResponseEntity<?> response = authController.login(validAuthHeader);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(FirebaseUser.class, response.getBody());

        FirebaseUser responseUser = (FirebaseUser) response.getBody();
        assertEquals("test-uid", responseUser.getUid());
        assertEquals("test@example.com", responseUser.getEmail());

        verify(authService).authenticateAdmin(VALID_TOKEN);
    }

    @Test
    void login_WithInvalidToken_ShouldReturnUnauthorized() {
        String errorMessage = "Invalid token";
        when(authService.authenticateAdmin(INVALID_TOKEN))
                .thenThrow(new RuntimeException(errorMessage));

        ResponseEntity<?> response = authController.login(AUTH_HEADER);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(errorMessage, errorResponse.getMessage());

        verify(authService).authenticateAdmin(INVALID_TOKEN);
    }

    @Test
    void login_WithNullErrorMessage_ShouldReturnDefaultMessage() {
        when(authService.authenticateAdmin(INVALID_TOKEN))
                .thenThrow(new RuntimeException());

        ResponseEntity<?> response = authController.login(AUTH_HEADER);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Authentication failed", errorResponse.getMessage());

        verify(authService).authenticateAdmin(INVALID_TOKEN);
    }

    @Test
    void login_WithMissingAuthorizationHeader_ShouldReturnUnauthorized() {
        ResponseEntity<?> response = authController.login(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
    }

    @Test
    void login_WithEmptyToken_ShouldReturnUnauthorized() {
        ResponseEntity<?> response = authController.login(BEARER_PREFIX);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Invalid authorization header format", errorResponse.getMessage());

        verifyNoInteractions(authService);
    }

    @Test
    void login_WithInvalidHeaderFormat_ShouldReturnUnauthorized() {
        String authHeader = "Invalid-Format";

        ResponseEntity<?> response = authController.login(authHeader);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Invalid authorization header format", errorResponse.getMessage());

        verifyNoInteractions(authService);
    }

    @Test
    void login_WithNullHeader_ShouldReturnUnauthorized() {
        ResponseEntity<?> response = authController.login(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Authorization header is missing or empty", errorResponse.getMessage());

        verifyNoInteractions(authService);
    }
}