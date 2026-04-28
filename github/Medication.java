package com.trithread.vitalmed.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "medications")
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private String medicationName;

    private String dosage;        // e.g., "500mg"
    private int dailyFrequency;   // e.g., 2 (twice daily)
    private LocalDateTime nextDoseTime;
    private String adherenceStatus; // "PENDING", "TAKEN", "LATE", "MISSED"

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.adherenceStatus = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public int getDailyFrequency() { return dailyFrequency; }
    public void setDailyFrequency(int dailyFrequency) { this.dailyFrequency = dailyFrequency; }

    public LocalDateTime getNextDoseTime() { return nextDoseTime; }
    public void setNextDoseTime(LocalDateTime nextDoseTime) { this.nextDoseTime = nextDoseTime; }

    public String getAdherenceStatus() { return adherenceStatus; }
    public void setAdherenceStatus(String adherenceStatus) { this.adherenceStatus = adherenceStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
