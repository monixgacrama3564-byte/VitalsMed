package com.trithread.vitalmed.controller;

import com.trithread.vitalmed.dto.AuthRequest;
import com.trithread.vitalmed.dto.AuthResponse;
import com.trithread.vitalmed.dto.RegisterRequest;
import com.trithread.vitalmed.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController — handles patient/provider registration and login.
 * Endpoints: POST /auth/register, POST /auth/login
 *
 * VitalsMed — TRITHREAD (Velayo, Gacrama, Tiro)
 * v0.1.0 — First Sprint
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * POST /auth/register
     * Register a new patient or medical provider.
     * Body: { fullName, email, password, role }
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /auth/login
     * Authenticate a user and return a JWT token.
     * Body: { email, password }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
