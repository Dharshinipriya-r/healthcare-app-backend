package com.hospital.Hospital.Management.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.Hospital.Management.dto.AnnouncementRequestDto;
import com.hospital.Hospital.Management.dto.DashboardAnalyticsDto;
import com.hospital.Hospital.Management.dto.RegisterRequest;
import com.hospital.Hospital.Management.model.DoctorAvailability;
import com.hospital.Hospital.Management.model.Role;
import com.hospital.Hospital.Management.model.SystemLog;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.repository.DoctorAvailabilityRepository;
import com.hospital.Hospital.Management.repository.SystemLogRepository;
import com.hospital.Hospital.Management.repository.UserRepository;
import com.hospital.Hospital.Management.service.AdminDashboardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final SystemLogRepository systemLogRepository;
    
    private final AdminDashboardService adminDashboardService;

   
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/users")
    public ResponseEntity<User> addUser(@Valid @RequestBody RegisterRequest request, @AuthenticationPrincipal UserDetails adminDetails) {
        User newUser = adminDashboardService.createUser(request, adminDetails);
        return ResponseEntity.status(201).body(newUser);
    }

    @PostMapping("/users/{userId}/block")
    public ResponseEntity<User> blockUser(@PathVariable Long userId, @AuthenticationPrincipal UserDetails adminDetails) {
        User updatedUser = adminDashboardService.blockUser(userId, adminDetails);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/users/{userId}/unblock")
    public ResponseEntity<User> unblockUser(@PathVariable Long userId, @AuthenticationPrincipal UserDetails adminDetails) {
        User updatedUser = adminDashboardService.unblockUser(userId, adminDetails);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/patients")
    public ResponseEntity<List<User>> getAllPatients() {
        List<User> patients = userRepository.findAll().stream()
                .filter(user -> user.getRoles().contains(Role.ROLE_PATIENT))
                .collect(Collectors.toList());
        return ResponseEntity.ok(patients);
    }

    // --- Doctor & Schedule Viewing Endpoints ---
    @GetMapping("/doctors")
    public ResponseEntity<List<User>> getAllDoctors() {
        List<User> doctors = userRepository.findAll().stream()
                .filter(user -> user.getRoles().contains(Role.ROLE_DOCTOR))
                .collect(Collectors.toList());
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/doctors/{doctorId}/schedule")
    public ResponseEntity<List<DoctorAvailability>> getDoctorSchedule(@PathVariable Long doctorId) {
        return ResponseEntity.ok(doctorAvailabilityRepository.findByDoctorId(doctorId));
    }

  

    // --- System Monitoring & Management Endpoints ---
    @GetMapping("/logs")
    public ResponseEntity<List<SystemLog>> getSystemLogs() {
        return ResponseEntity.ok(systemLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp")));
    }



    @GetMapping("/analytics")
    public ResponseEntity<DashboardAnalyticsDto> getDashboardAnalytics() {
        DashboardAnalyticsDto analytics = adminDashboardService.getDashboardAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @PostMapping("/announcements")
    public ResponseEntity<Void> sendAnnouncement(
            @Valid @RequestBody AnnouncementRequestDto announcement,
            @AuthenticationPrincipal UserDetails adminDetails
    ) {
        adminDashboardService.sendSystemAnnouncement(announcement, adminDetails);
        return ResponseEntity.ok().build();
    }
}