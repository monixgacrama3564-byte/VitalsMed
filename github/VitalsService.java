package com.trithread.vitalmed.service;

import com.trithread.vitalmed.model.Patient;
import com.trithread.vitalmed.model.VitalsLog;
import com.trithread.vitalmed.repository.PatientRepository;
import com.trithread.vitalmed.repository.VitalsLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * VitalsService — biometric data ingestion with Safe Zone validation.
 *
 * Safe Zones (v0.1.0):
 *   Heart Rate    : 40–200 bpm   (human-possible range)
 *                  Normal zone   : 60–100 bpm
 *                  Alert zone    : < 60 or > 100 bpm
 *
 *   Body Temp     : 35.0–42.0°C  (human-possible range)
 *                  Normal zone   : 36.1–37.5°C
 *                  Alert zone    : < 36.1 or > 37.5°C
 *
 * alertStatus values: "NORMAL" | "WARNING" | "CRITICAL"
 */
@Service
public class VitalsService {

    // ── Human-possible bounds (reject outside these) ──────────────────────────
    private static final double HR_MIN_POSSIBLE  = 40.0;
    private static final double HR_MAX_POSSIBLE  = 200.0;
    private static final double TEMP_MIN_POSSIBLE = 35.0;
    private static final double TEMP_MAX_POSSIBLE = 42.0;

    // ── Safe zone thresholds (trigger WARNING if outside) ────────────────────
    private static final double HR_SAFE_LOW   = 60.0;
    private static final double HR_SAFE_HIGH  = 100.0;
    private static final double TEMP_SAFE_LOW  = 36.1;
    private static final double TEMP_SAFE_HIGH = 37.5;

    @Autowired
    private VitalsLogRepository vitalsLogRepository;

    @Autowired
    private PatientRepository patientRepository;

    // ─── LOG VITALS ──────────────────────────────────────────────────────────
    public VitalsLog logVitals(VitalsLog vitalsLog) {
        validateVitals(vitalsLog.getHeartRate(), vitalsLog.getBodyTemperature());

        Patient patient = patientRepository.findById(vitalsLog.getPatient().getId())
                .orElseThrow(() -> new RuntimeException("Patient not found."));

        vitalsLog.setPatient(patient);
        vitalsLog.setAlertStatus(determineAlertStatus(
                vitalsLog.getHeartRate(),
                vitalsLog.getBodyTemperature()
        ));

        return vitalsLogRepository.save(vitalsLog);
    }

    // ─── GET HISTORY ─────────────────────────────────────────────────────────
    public List<VitalsLog> getVitalsByPatient(Long patientId) {
        return vitalsLogRepository.findByPatientIdOrderByLoggedAtDesc(patientId);
    }

    // ─── VALIDATION ──────────────────────────────────────────────────────────
    /**
     * Rejects data outside human-possible ranges.
     * e.g., heartRate=0 or heartRate=500 will throw IllegalArgumentException.
     */
    private void validateVitals(double heartRate, double bodyTemperature) {
        if (heartRate < HR_MIN_POSSIBLE || heartRate > HR_MAX_POSSIBLE) {
            throw new IllegalArgumentException(
                String.format("Heart rate %.1f bpm is outside human-possible range (%.0f–%.0f bpm).",
                        heartRate, HR_MIN_POSSIBLE, HR_MAX_POSSIBLE));
        }
        if (bodyTemperature < TEMP_MIN_POSSIBLE || bodyTemperature > TEMP_MAX_POSSIBLE) {
            throw new IllegalArgumentException(
                String.format("Body temperature %.1f°C is outside human-possible range (%.1f–%.1f°C).",
                        bodyTemperature, TEMP_MIN_POSSIBLE, TEMP_MAX_POSSIBLE));
        }
    }

    // ─── ALERT STATUS ────────────────────────────────────────────────────────
    /**
     * Maps vitals readings to alert levels.
     *   NORMAL   → both HR and Temp within safe zones
     *   WARNING  → one or both outside safe zone but within possible range
     *   CRITICAL → extreme values near the boundary of human-possible range
     */
    private String determineAlertStatus(double heartRate, double bodyTemperature) {
        boolean hrCritical   = heartRate < 50 || heartRate > 150;
        boolean tempCritical = bodyTemperature < 35.5 || bodyTemperature > 40.0;

        if (hrCritical || tempCritical) return "CRITICAL";

        boolean hrWarning   = heartRate < HR_SAFE_LOW || heartRate > HR_SAFE_HIGH;
        boolean tempWarning = bodyTemperature < TEMP_SAFE_LOW || bodyTemperature > TEMP_SAFE_HIGH;

        if (hrWarning || tempWarning) return "WARNING";

        return "NORMAL";
    }
}
