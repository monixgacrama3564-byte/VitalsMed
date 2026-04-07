package com.trithread.vitalsmed.repository;

import com.trithread.vitalsmed.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {}
