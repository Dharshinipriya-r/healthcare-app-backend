package com.hospital.Hospital.Management.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.Hospital.Management.dto.AdminUserCreationRequest;
import com.hospital.Hospital.Management.dto.UserProfileResponse;
import com.hospital.Hospital.Management.service.AdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/profile")
    public ResponseEntity<String> adminProfile(Principal principal) {
        return ResponseEntity.ok("Admin profile - " + principal.getName());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<String> adminDashboard() {
        return ResponseEntity.ok("Admin dashboard accessed successfully");
    }

    @PostMapping("/add-doctor")
    public ResponseEntity<UserProfileResponse> addDoctor(@Valid @RequestBody AdminUserCreationRequest request) {
        return ResponseEntity.ok(adminService.createDoctor(request));
    }

   
    @PostMapping("/add-admin")
    public ResponseEntity<UserProfileResponse> addAdmin(@Valid @RequestBody AdminUserCreationRequest request) {
        return ResponseEntity.ok(adminService.createAdmin(request));
    }
}
