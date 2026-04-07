package com.trithread.vitalsmed.service;

import com.trithread.vitalsmed.model.Patient;
import com.trithread.vitalsmed.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Patient register(Patient patient) {
        patient.setPassword(passwordEncoder.encode(patient.getPassword()));
        return patientRepository.save(patient);
    }

    public Patient login(String email, String rawPassword) {
        return patientRepository.findByEmail(email)
                .filter(p -> passwordEncoder.matches(rawPassword, p.getPassword()))
                .orElse(null);
    }
}