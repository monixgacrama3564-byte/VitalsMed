package com.trithread.vitalmed.controller;

import com.trithread.vitalmed.model.VitalsLog;
import com.trithread.vitalmed.service.VitalsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * VitalsController — handles biometric data ingestion and retrieval.
 *
 * POST /vitals/log         — log a new vitals reading
 * GET  /vitals/{patientId} — retrieve vitals history for a patient
 *
 * Validation rules (Safe Zones):
 *   Heart Rate    : 40–200 bpm  (alert if < 60 or > 100)
 *   Body Temp     : 35.0–42.0°C (alert if < 36.1 or > 37.5)
 *
 * VitalsMed — TRITHREAD (Velayo, Gacrama, Tiro)
 * v0.1.0 — First Sprint
 */
@RestController
@RequestMapping("/vitals")
@CrossOrigin(origins = "*")
public class VitalsController {

    @Autowired
    private VitalsService vitalsService;

    /**
     * POST /vitals/log
     * Log a new biometric reading for a patient.
     * Validates that data is within human-possible range.
     * Automatically assigns alertStatus based on safe-zone thresholds.
     * Body: { patientId, heartRate, bodyTemperature }
     */
    @PostMapping("/log")
    public ResponseEntity<?> logVitals(@RequestBody VitalsLog vitalsLog) {
        try {
            VitalsLog saved = vitalsService.logVitals(vitalsLog);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /vitals/{patientId}
     * Returns full vitals history for a given patient.
     * RLS enforced — providers can only access their assigned patients.
     */
    @GetMapping("/{patientId}")
    public ResponseEntity<List<VitalsLog>> getVitalsHistory(@PathVariable Long patientId) {
        List<VitalsLog> logs = vitalsService.getVitalsByPatient(patientId);
        return ResponseEntity.ok(logs);
    }
}
