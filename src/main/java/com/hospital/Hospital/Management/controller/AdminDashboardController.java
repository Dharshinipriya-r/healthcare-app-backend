package com.hospital.Hospital.Management.controller;

import com.hospital.Hospital.Management.dto.*;
import com.hospital.Hospital.Management.model.*;
import com.hospital.Hospital.Management.repository.DoctorAvailabilityRepository;
import com.hospital.Hospital.Management.repository.FeedbackRepository;
import com.hospital.Hospital.Management.repository.SystemLogRepository;
import com.hospital.Hospital.Management.repository.UserRepository;
import com.hospital.Hospital.Management.service.AdminDashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final SystemLogRepository systemLogRepository;
    private final FeedbackRepository feedbackRepository;
    private final AdminDashboardService adminDashboardService;

    // --- User Management Endpoints ---
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

    // --- Feedback and Ratings Endpoints ---
    @GetMapping("/doctors/{doctorId}/feedback")
    public ResponseEntity<List<Feedback>> getDoctorFeedback(@PathVariable Long doctorId) {
        List<Feedback> feedback = adminDashboardService.getFeedbackForDoctor(doctorId);
        return ResponseEntity.ok(feedback);
    }

    @GetMapping("/feedback/unreviewed")
    public ResponseEntity<List<Feedback>> getUnreviewedFeedback() {
        List<Feedback> feedback = adminDashboardService.getUnreviewedFeedback();
        return ResponseEntity.ok(feedback);
    }

    @PostMapping("/feedback/{feedbackId}/review")
    public ResponseEntity<Feedback> reviewFeedback(
            @PathVariable Long feedbackId,
            @RequestBody AdminNotesDto notes,
            @AuthenticationPrincipal UserDetails adminDetails
    ) {
        Feedback reviewedFeedback = adminDashboardService.reviewFeedback(feedbackId, notes.getAdminNotes(), adminDetails);
        return ResponseEntity.ok(reviewedFeedback);
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