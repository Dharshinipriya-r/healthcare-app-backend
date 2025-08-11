package com.hospital.Hospital.Management.controller;

import com.hospital.Hospital.Management.dto.ApiResponse;
import com.hospital.Hospital.Management.dto.WaitlistEntryDto;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.repository.UserRepository;
import com.hospital.Hospital.Management.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WaitlistController {

    private final WaitlistService waitlistService;
    private final UserRepository userRepository;

    @PostMapping("/doctors/{doctorId}/waitlist/join")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<WaitlistEntryDto>> joinWaitlist(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate preferredDate,
            Principal principal) {

        WaitlistEntryDto newEntry = waitlistService.joinWaitlist(doctorId, principal.getName(), preferredDate);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "You have been successfully added to the waitlist for " + preferredDate, newEntry));
    }

    @GetMapping("/doctors/{id}/waitlist")
    @PreAuthorize("hasRole('DOCTOR') and #id == @userRepository.findByEmail(principal.username).orElseThrow().getId()")
    public ResponseEntity<List<WaitlistEntryDto>> getDoctorWaitlist(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<WaitlistEntryDto> waitlist = waitlistService.getWaitlistForDoctor(id, date);
        return ResponseEntity.ok(waitlist);
    }

    @PostMapping("/waitlist/{waitlistId}/notify")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<Void>> notifyPatientFromWaitlist(
            @PathVariable Long waitlistId,
            Principal principal) {

        User doctor = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new NoSuchElementException("Authenticated doctor not found"));

        waitlistService.notifyWaitlistedPatient(waitlistId, doctor.getId());

        return ResponseEntity.ok(new ApiResponse<>(true, "Notification sent to waitlisted patient.", null));
    }
}