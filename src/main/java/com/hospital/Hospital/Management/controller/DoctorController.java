package com.hospital.Hospital.Management.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/doctor")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorController {

    @GetMapping("/profile")
    public ResponseEntity<String> doctorProfile(Principal principal) {
        return ResponseEntity.ok("Doctor profile - " + principal.getName());
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<String> doctorDashboard() {
        return ResponseEntity.ok("Doctor dashboard accessed successfully");
    }
}
