package com.hospital.Hospital.Management.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/patient")
@PreAuthorize("hasRole('PATIENT')")
public class PatientController {

    @GetMapping("/profile")
    public ResponseEntity<String> patientProfile(Principal principal) {
        return ResponseEntity.ok("Patient profile - " + principal.getName());
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<String> patientDashboard() {
        return ResponseEntity.ok("Patient dashboard accessed successfully");
    }
}
