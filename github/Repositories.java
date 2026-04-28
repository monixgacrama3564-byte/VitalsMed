package com.trithread.vitalmed.repository;

import com.trithread.vitalmed.model.Medication;
import com.trithread.vitalmed.model.Patient;
import com.trithread.vitalmed.model.VitalsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// ─── Patient Repository ───────────────────────────────────────────────────────
@Repository
interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByEmail(String email);
}

// ─── Medication Repository ────────────────────────────────────────────────────
@Repository
interface MedicationRepository extends JpaRepository<Medication, Long> {
    List<Medication> findByPatientId(Long patientId);
}

// ─── VitalsLog Repository ─────────────────────────────────────────────────────
@Repository
interface VitalsLogRepository extends JpaRepository<VitalsLog, Long> {
    // Returns logs newest-first for timeline display
    List<VitalsLog> findByPatientIdOrderByLoggedAtDesc(Long patientId);
}
