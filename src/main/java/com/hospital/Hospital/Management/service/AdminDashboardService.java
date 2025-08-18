package com.hospital.Hospital.Management.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.Hospital.Management.dto.AnnouncementRequestDto;
import com.hospital.Hospital.Management.dto.DashboardAnalyticsDto;
import com.hospital.Hospital.Management.dto.RegisterRequest;
import com.hospital.Hospital.Management.exception.UserAlreadyExistsException;
import com.hospital.Hospital.Management.model.Appointment;
import com.hospital.Hospital.Management.model.AppointmentStatus;
import com.hospital.Hospital.Management.model.Role;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.repository.AppointmentRepository;
import com.hospital.Hospital.Management.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final LogService logService;
    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

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


  

    private Role mapRole(String role) {
        return switch (role.toUpperCase()) {
            case "ADMIN" -> Role.ROLE_ADMIN;
            case "DOCTOR" -> Role.ROLE_DOCTOR;
            case "PATIENT" -> Role.ROLE_PATIENT;
            default -> throw new IllegalArgumentException("Invalid role specified: " + role);
        };
    }
}