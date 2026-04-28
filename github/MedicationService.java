package com.trithread.vitalmed.service;

import com.trithread.vitalmed.model.Medication;
import com.trithread.vitalmed.model.Patient;
import com.trithread.vitalmed.repository.MedicationRepository;
import com.trithread.vitalmed.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MedicationService — CRUD operations for patient medication schedules.
 *
 * Adherence Window Logic (v0.1.0 "Life-Link Patch"):
 *   - When a medication is added, nextDoseTime is auto-calculated.
 *   - Every (24 / dailyFrequency) hours = interval between doses.
 *   - If "Taken" is NOT confirmed within 2 hours of nextDoseTime → status becomes LATE.
 */
@Service
public class MedicationService {

    private static final int ADHERENCE_WINDOW_HOURS = 2;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private PatientRepository patientRepository;

    // ─── READ ───────────────────────────────────────────────────────────────────
    public List<Medication> getMedicationsByPatient(Long patientId) {
        // Refresh adherence statuses before returning
        List<Medication> meds = medicationRepository.findByPatientId(patientId);
        meds.forEach(this::checkAndFlagLate);
        return meds;
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────────
    public Medication createMedication(Medication medication) {
        Patient patient = patientRepository.findById(medication.getPatient().getId())
                .orElseThrow(() -> new RuntimeException("Patient not found."));

        medication.setPatient(patient);
        medication.setAdherenceStatus("PENDING");
        medication.setNextDoseTime(calculateNextDose(medication.getDailyFrequency()));

        return medicationRepository.save(medication);
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────────
    public Medication updateMedication(Long id, Medication updatedData) {
        Medication existing = medicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication not found."));

        existing.setMedicationName(updatedData.getMedicationName());
        existing.setDosage(updatedData.getDosage());
        existing.setDailyFrequency(updatedData.getDailyFrequency());
        // Recalculate next dose time when frequency changes
        existing.setNextDoseTime(calculateNextDose(updatedData.getDailyFrequency()));

        return medicationRepository.save(existing);
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────────
    public void deleteMedication(Long id) {
        if (!medicationRepository.existsById(id)) {
            throw new RuntimeException("Medication not found.");
        }
        medicationRepository.deleteById(id);
    }

    // ─── MARK TAKEN ──────────────────────────────────────────────────────────────
    /**
     * Mark a dose as TAKEN.
     * If the confirmation arrives within the 2-hour adherence window → TAKEN.
     * If it arrives after the window has closed → LATE.
     * Then schedule the next dose automatically.
     */
    public Medication markDoseTaken(Long id) {
        Medication med = medicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication not found."));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowDeadline = med.getNextDoseTime().plusHours(ADHERENCE_WINDOW_HOURS);

        if (now.isAfter(windowDeadline)) {
            med.setAdherenceStatus("LATE");
        } else {
            med.setAdherenceStatus("TAKEN");
        }

        // Schedule the next dose
        med.setNextDoseTime(calculateNextDose(med.getDailyFrequency()));

        return medicationRepository.save(med);
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────────

    /**
     * Auto-flag as LATE if adherence window has passed without a TAKEN confirmation.
     */
    private void checkAndFlagLate(Medication med) {
        if ("PENDING".equals(med.getAdherenceStatus()) && med.getNextDoseTime() != null) {
            LocalDateTime deadline = med.getNextDoseTime().plusHours(ADHERENCE_WINDOW_HOURS);
            if (LocalDateTime.now().isAfter(deadline)) {
                med.setAdherenceStatus("LATE");
                medicationRepository.save(med);
            }
        }
    }

    /**
     * Calculate when the next dose should occur based on daily frequency.
     * e.g., dailyFrequency=2 → every 12 hours from now.
     */
    private LocalDateTime calculateNextDose(int dailyFrequency) {
        if (dailyFrequency <= 0) throw new IllegalArgumentException("Daily frequency must be > 0.");
        long intervalHours = 24L / dailyFrequency;
        return LocalDateTime.now().plusHours(intervalHours);
    }
}
