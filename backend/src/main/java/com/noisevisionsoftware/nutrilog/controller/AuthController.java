package com.noisevisionsoftware.nutrilog.controller;

import com.noisevisionsoftware.nutrilog.dto.response.ErrorResponse;
import com.noisevisionsoftware.nutrilog.security.model.FirebaseUser;
import com.noisevisionsoftware.nutrilog.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || authHeader.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body( new ErrorResponse("Authorization header is missing or empty"));
            }

            if (!authHeader.startsWith("Bearer ") || authHeader.length() == 7) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Invalid authorization header format"));
            }

            String token = authHeader.substring(7);

            if (token.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Token is empty"));
            }

            FirebaseUser user = authService.authenticateAdmin(token);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(e.getMessage() != null ? e.getMessage() : "Authentication failed"));
        }
    }
}
