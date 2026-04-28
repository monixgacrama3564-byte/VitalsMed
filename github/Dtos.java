package com.trithread.vitalmed.dto;

// ─── Login Request ────────────────────────────────────────────────────────────
// POST /auth/login body
class AuthRequest {
    private String email;
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

// ─── Register Request ─────────────────────────────────────────────────────────
// POST /auth/register body
class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private String role; // "PATIENT" or "PROVIDER"

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

// ─── Auth Response ────────────────────────────────────────────────────────────
// Returned from both /login and /register
class AuthResponse {
    private String token;
    private Long patientId;
    private String fullName;
    private String role;

    public AuthResponse(String token, Long patientId, String fullName, String role) {
        this.token = token;
        this.patientId = patientId;
        this.fullName = fullName;
        this.role = role;
    }

    public String getToken() { return token; }
    public Long getPatientId() { return patientId; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
}
