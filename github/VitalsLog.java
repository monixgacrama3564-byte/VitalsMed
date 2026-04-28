package com.trithread.vitalmed.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vitals_log")
public class VitalsLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    private double heartRate;       // bpm — valid range: 40–200
    private double bodyTemperature; // Celsius — valid range: 35.0–42.0

    private String alertStatus;     // "NORMAL", "WARNING", "CRITICAL"
    private LocalDateTime loggedAt;

    @PrePersist
    protected void onCreate() {
        this.loggedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public double getHeartRate() { return heartRate; }
    public void setHeartRate(double heartRate) { this.heartRate = heartRate; }

    public double getBodyTemperature() { return bodyTemperature; }
    public void setBodyTemperature(double bodyTemperature) { this.bodyTemperature = bodyTemperature; }

    public String getAlertStatus() { return alertStatus; }
    public void setAlertStatus(String alertStatus) { this.alertStatus = alertStatus; }

    public LocalDateTime getLoggedAt() { return loggedAt; }
    public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }
}
