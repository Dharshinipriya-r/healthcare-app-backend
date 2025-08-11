package com.hospital.Hospital.Management.service;

import com.hospital.Hospital.Management.dto.AnnouncementRequestDto;
import com.hospital.Hospital.Management.dto.DashboardAnalyticsDto;
import com.hospital.Hospital.Management.dto.RegisterRequest;
import com.hospital.Hospital.Management.exception.UserAlreadyExistsException;
import com.hospital.Hospital.Management.model.*;
import com.hospital.Hospital.Management.repository.AppointmentRepository;
import com.hospital.Hospital.Management.repository.FeedbackRepository;
import com.hospital.Hospital.Management.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final LogService logService;
    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final FeedbackRepository feedbackRepository;

    @Transactional
    public User blockUser(Long userId, UserDetails adminDetails) {
        log.info("Attempting to block user with ID: {}", userId);
        User userToBlock = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        userToBlock.setAccountNonLocked(false);
        User updatedUser = userRepository.save(userToBlock);
        User adminUser = userRepository.findByEmail(adminDetails.getUsername()).orElse(null);
        String details = String.format("User '%s' (ID: %d) was BLOCKED.", updatedUser.getEmail(), updatedUser.getId());
        logService.logActivity(adminUser, "USER_BLOCKED", details);
        log.info("Successfully blocked user with ID: {}", userId);
        return updatedUser;
    }

    @Transactional
    public User unblockUser(Long userId, UserDetails adminDetails) {
        log.info("Attempting to unblock user with ID: {}", userId);
        User userToUnblock = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        userToUnblock.setAccountNonLocked(true);
        User updatedUser = userRepository.save(userToUnblock);
        User adminUser = userRepository.findByEmail(adminDetails.getUsername()).orElse(null);
        String details = String.format("User '%s' (ID: %d) was UNBLOCKED.", updatedUser.getEmail(), updatedUser.getId());
        logService.logActivity(adminUser, "USER_UNBLOCKED", details);
        log.info("Successfully unblocked user with ID: {}", userId);
        return updatedUser;
    }

    @Transactional
    public User createUser(RegisterRequest request, UserDetails adminDetails) {
        log.info("Admin '{}' is creating a new user with email: {}", adminDetails.getUsername(), request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Cannot create user. Email " + request.getEmail() + " is already in use.");
        }
        User newUser = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))

                .enabled(true)
                .build();
        User savedUser = userRepository.save(newUser);
        User adminUser = userRepository.findByEmail(adminDetails.getUsername()).orElse(null);
        String logDetails = String.format("Admin created new user '%s' (ID: %d) with role %s.",
                savedUser.getEmail(), savedUser.getId());
        logService.logActivity(adminUser, "USER_CREATED_BY_ADMIN", logDetails);
        return savedUser;
    }

    public DashboardAnalyticsDto getDashboardAnalytics() {
        log.info("Fetching dashboard analytics data.");
        List<User> allUsers = userRepository.findAll();
        List<Appointment> allAppointments = appointmentRepository.findAll();
        long totalUsers = allUsers.size();
        long totalDoctors = allUsers.stream().filter(user -> user.getRoles().contains(Role.ROLE_DOCTOR)).count();
        long totalPatients = allUsers.stream().filter(user -> user.getRoles().contains(Role.ROLE_PATIENT)).count();
        long totalAppointmentsCount = allAppointments.size();
        long scheduledAppointments = allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED).count();
        long completedAppointments = allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
        long canceledAppointments = allAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CANCELLED_BY_DOCTOR || a.getStatus() == AppointmentStatus.CANCELLED_BY_PATIENT)
                .count();
        return DashboardAnalyticsDto.builder()
                .totalUsers(totalUsers)
                .totalDoctors(totalDoctors)
                .totalPatients(totalPatients)
                .totalAppointments(totalAppointmentsCount)
                .scheduledAppointments(scheduledAppointments)
                .completedAppointments(completedAppointments)
                .canceledAppointments(canceledAppointments)
                .build();
    }

    public void sendSystemAnnouncement(AnnouncementRequestDto announcement, UserDetails adminDetails) {
        log.info("Admin '{}' is sending a system-wide announcement with subject: '{}'",
                adminDetails.getUsername(), announcement.getSubject());
        List<User> usersToSend = userRepository.findAll().stream()
                .filter(user -> user.isEnabled() && user.isAccountNonLocked())
                .toList();
        for (User user : usersToSend) {
            try {
                emailService.sendGenericEmail(user.getEmail(), announcement.getSubject(), announcement.getMessage());
            } catch (Exception e) {
                log.error("Failed to send announcement to {}: {}", user.getEmail(), e.getMessage());
            }
        }
        User adminUser = userRepository.findByEmail(adminDetails.getUsername()).orElse(null);
        String logDetails = String.format("Sent announcement with subject: '%s' to %d users.",
                announcement.getSubject(), usersToSend.size());
        logService.logActivity(adminUser, "SYSTEM_ANNOUNCEMENT_SENT", logDetails);
        log.info("Finished processing announcement request for {} users.", usersToSend.size());
    }

    public List<Feedback> getFeedbackForDoctor(Long doctorId) {
        log.info("Fetching all feedback for doctor ID: {}", doctorId);
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with ID: " + doctorId));
        return feedbackRepository.findByDoctorOrderByCreatedAtDesc(doctor);
    }

    public List<Feedback> getUnreviewedFeedback() {
        log.info("Fetching all unreviewed feedback for administrative review.");
        return feedbackRepository.findByIsReviewedOrderByCreatedAtAsc(false);
    }

    @Transactional
    public Feedback reviewFeedback(Long feedbackId, String adminNotes, UserDetails adminDetails) {
        log.info("Admin '{}' is reviewing feedback ID: {}", adminDetails.getUsername(), feedbackId);
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new EntityNotFoundException("Feedback not found with ID: " + feedbackId));
        feedback.setIsReviewed(true);
        feedback.setAdminNotes(adminNotes);
        Feedback savedFeedback = feedbackRepository.save(feedback);
        User adminUser = userRepository.findByEmail(adminDetails.getUsername()).orElse(null);
        String logDetails = String.format("Feedback ID %d was marked as reviewed.", feedbackId);
        logService.logActivity(adminUser, "FEEDBACK_REVIEWED", logDetails);
        return savedFeedback;
    }

    private Role mapRole(String role) {
        return switch (role.toUpperCase()) {
            case "ADMIN" -> Role.ROLE_ADMIN;
            case "DOCTOR" -> Role.ROLE_DOCTOR;
            case "PATIENT" -> Role.ROLE_PATIENT;
            default -> throw new IllegalArgumentException("Invalid role specified: " + role);
        };
    }
}