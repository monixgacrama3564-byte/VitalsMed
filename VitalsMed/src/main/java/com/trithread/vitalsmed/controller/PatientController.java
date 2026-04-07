package com.trithread.vitalsmed.controller;

import com.trithread.vitalsmed.model.Patient;
import com.trithread.vitalsmed.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @PostMapping("/register")
    public Patient register(@RequestBody Patient patient) {
        return patientService.register(patient);
    }

    @PostMapping("/login")
    public String login(@RequestBody Patient patient) {
        Patient loggedIn = patientService.login(patient.getEmail(), patient.getPassword());
        return loggedIn != null ? "Login successful" : "Invalid credentials";
    }
}