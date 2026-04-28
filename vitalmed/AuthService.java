package com.trithread.vitalmed.service;

import com.trithread.vitalmed.dto.AuthRequest;
import com.trithread.vitalmed.dto.AuthResponse;
import com.trithread.vitalmed.dto.RegisterRequest;
import com.trithread.vitalmed.model.Patient;
import com.trithread.vitalmed.repository.PatientRepository;
import com.trithread.vitalmed.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthService — handles registration and login logic.
 * Passwords are hashed with BCrypt before storage.
 * JWT is issued on successful auth.
 */
@Service
public class AuthService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthResponse register(RegisterRequest request) {
        if (patientRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use.");
        }

        Patient patient = new Patient();
        patient.setFullName(request.getFullName());
        patient.setEmail(request.getEmail());
        patient.setPassword(encoder.encode(request.getPassword()));
        patient.setRole(request.getRole() != null ? request.getRole() : "PATIENT");

        patientRepository.save(patient);

        String token = jwtUtil.generateToken(patient.getEmail(), patient.getRole());
        return new AuthResponse(token, patient.getId(), patient.getFullName(), patient.getRole());
    }

    public AuthResponse login(AuthRequest request) {
        Patient patient = patientRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));

        if (!encoder.matches(request.getPassword(), patient.getPassword())) {
            throw new RuntimeException("Invalid email or password.");
        }

        String token = jwtUtil.generateToken(patient.getEmail(), patient.getRole());
        return new AuthResponse(token, patient.getId(), patient.getFullName(), patient.getRole());
    }
}
