package com.trithread.vitalmed.controller;

import com.trithread.vitalmed.model.Medication;
import com.trithread.vitalmed.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MedicationController — full CRUD for patient medication schedules.
 *
 * GET    /patient/medications          — list all medications for a patient
 * POST   /patient/medications          — add a new medication
 * PUT    /patient/medications/{id}     — update medication info
 * DELETE /patient/medications/{id}     — remove a medication
 *
 * VitalsMed — TRITHREAD (Velayo, Gacrama, Tiro)
 * v0.1.0 — First Sprint
 */
@RestController
@RequestMapping("/patient/medications")
@CrossOrigin(origins = "*")
public class MedicationController {

    @Autowired
    private MedicationService medicationService;

    /**
     * GET /patient/medications
     * Returns all medications for the authenticated patient.
     * Adheres to RLS — only returns records assigned to the requester's patient ID.
     */
    @GetMapping
    public ResponseEntity<List<Medication>> getAllMedications(@RequestParam Long patientId) {
        List<Medication> medications = medicationService.getMedicationsByPatient(patientId);
        return ResponseEntity.ok(medications);
    }

    /**
     * POST /patient/medications
     * Adds a new medication to the patient's schedule.
     * Automatically calculates nextDoseTime based on dailyFrequency.
     * Body: { patientId, medicationName, dosage, dailyFrequency }
     */
    @PostMapping
    public ResponseEntity<Medication> createMedication(@RequestBody Medication medication) {
        Medication created = medicationService.createMedication(medication);
        return ResponseEntity.ok(created);
    }

    /**
     * PUT /patient/medications/{id}
     * Updates an existing medication record.
     * Body: { medicationName, dosage, dailyFrequency }
     */
    @PutMapping("/{id}")
    public ResponseEntity<Medication> updateMedication(
            @PathVariable Long id,
            @RequestBody Medication updatedData) {
        Medication updated = medicationService.updateMedication(id, updatedData);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /patient/medications/{id}
     * Removes a medication from the patient's schedule.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedication(@PathVariable Long id) {
        medicationService.deleteMedication(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /patient/medications/{id}/taken
     * Mark a dose as TAKEN — resets the adherence window.
     * If called within 2-hour window: status → TAKEN
     * If called after 2-hour window: status → LATE
     */
    @PatchMapping("/{id}/taken")
    public ResponseEntity<Medication> markAsTaken(@PathVariable Long id) {
        Medication updated = medicationService.markDoseTaken(id);
        return ResponseEntity.ok(updated);
    }
}
