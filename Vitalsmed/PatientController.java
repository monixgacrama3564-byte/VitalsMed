package com.trithread.vitalsmed.controller;

import com.trithread.vitalsmed.model.Patient;
import com.trithread.vitalsmed.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients")
public class PatientController {

    @Autowired
    private PatientRepository repo;

    @PostMapping
    public Patient create(@RequestBody Patient p) {
        return repo.save(p);
    }

    @GetMapping
    public List<Patient> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Patient getById(@PathVariable Long id) {
        return repo.findById(id).orElseThrow();
    }

    @PutMapping("/{id}")
    public Patient update(@PathVariable Long id, @RequestBody Patient newData) {
        Patient p = repo.findById(id).orElseThrow();
        p.setName(newData.getName());
        return repo.save(p);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        repo.deleteById(id);
        return "Deleted successfully";
    }
}
