package com.hospital.Hospital.Management.service;

import com.hospital.Hospital.Management.dto.WaitlistEntryDto;
import com.hospital.Hospital.Management.exception.ResourceNotFoundException;
import com.hospital.Hospital.Management.model.Role;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.model.WaitlistEntry;
import com.hospital.Hospital.Management.repository.UserRepository;
import com.hospital.Hospital.Management.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public WaitlistEntryDto joinWaitlist(Long doctorId, String patientEmail, LocalDate preferredDate) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with email: " + patientEmail));
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

        WaitlistEntry entry = WaitlistEntry.builder()
                .patient(patient)
                .doctor(doctor)
                .preferredDate(preferredDate)
                .build();

        WaitlistEntry savedEntry = waitlistRepository.save(entry);
        return mapToDto(savedEntry);
    }

    @Transactional(readOnly = true)
    public List<WaitlistEntryDto> getWaitlistForDoctor(Long doctorId, LocalDate date) {
        User doctor = userRepository.findById(doctorId)
                .filter(user -> user.getRoles().contains(Role.ROLE_DOCTOR))
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

        List<WaitlistEntry> entries = waitlistRepository.findByDoctorAndPreferredDateOrderByCreatedAtAsc(doctor, date);

        return entries.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public void notifyWaitlistedPatient(Long waitlistId, Long doctorId) {
        log.info("Doctor {} is notifying waitlisted patient from entry {}", doctorId, waitlistId);

        WaitlistEntry entry = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist entry not found with ID: " + waitlistId));

        if (!entry.getDoctor().getId().equals(doctorId)) {
            throw new SecurityException("Doctor is not authorized to manage this waitlist entry.");
        }

        User patient = entry.getPatient();
        User doctor = entry.getDoctor();

        try {
            emailService.sendWaitlistNotificationEmail(patient, doctor, entry.getPreferredDate());
            log.info("Successfully sent waitlist notification to patient {}", patient.getEmail());
        } catch (Exception e) {
            log.error("Failed to send waitlist notification for entry {}", waitlistId, e);
        }

        waitlistRepository.delete(entry);
        log.info("Removed patient {} from the waitlist for Dr. {} on {}",
                patient.getFullName(), doctor.getFullName(), entry.getPreferredDate());
    }

    private WaitlistEntryDto mapToDto(WaitlistEntry entry) {
        return WaitlistEntryDto.builder()
                .waitlistId(entry.getId())
                .patientId(entry.getPatient().getId())
                .patientName(entry.getPatient().getFullName())
                .preferredDate(entry.getPreferredDate())
                .requestedAt(entry.getCreatedAt())
                .build();
    }
}