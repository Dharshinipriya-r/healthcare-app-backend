package com.hospital.Hospital.Management.controller;

import com.hospital.Hospital.Management.dto.AdminUserCreationRequest;
import com.hospital.Hospital.Management.dto.UserProfileResponse;
import com.hospital.Hospital.Management.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

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

    /**
     * Endpoint for admin to create a doctor user
     * @param request The doctor creation request
     * @return Response with created doctor information
     */
    @PostMapping("/add-doctor")
    public ResponseEntity<UserProfileResponse> addDoctor(@Valid @RequestBody AdminUserCreationRequest request) {
        return ResponseEntity.ok(adminService.createDoctor(request));
    }

    /**
     * Endpoint for admin to create another admin user
     * @param request The admin creation request
     * @return Response with created admin information
     */
    @PostMapping("/add-admin")
    public ResponseEntity<UserProfileResponse> addAdmin(@Valid @RequestBody AdminUserCreationRequest request) {
        return ResponseEntity.ok(adminService.createAdmin(request));
    }
}
