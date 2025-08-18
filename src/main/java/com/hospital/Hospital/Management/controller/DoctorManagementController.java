package com.hospital.Hospital.Management.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.Hospital.Management.dto.AddNoteResponseDto;
import com.hospital.Hospital.Management.dto.AppointmentActionResponseDto;
import com.hospital.Hospital.Management.dto.AppointmentHistoryDto;
import com.hospital.Hospital.Management.dto.AppointmentResponseDto;
import com.hospital.Hospital.Management.dto.ConsultationNoteDto;
import com.hospital.Hospital.Management.dto.DoctorProfileDto;
import com.hospital.Hospital.Management.dto.RescheduleRequestDto;
import com.hospital.Hospital.Management.dto.SetAvailabilityResponseDto;
import com.hospital.Hospital.Management.dto.WeeklyAvailabilityRequestDto;
import com.hospital.Hospital.Management.model.AppointmentStatus;
import com.hospital.Hospital.Management.service.DoctorManagementService;

import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/doctors")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorManagementController {

    private final DoctorManagementService doctorService;

    public DoctorManagementController(DoctorManagementService doctorService) {
        this.doctorService = doctorService;
    }

    @PutMapping("/{id}/profile")
    @PreAuthorize("#id == @userRepository.findByEmail(principal.username).get().id")
    public ResponseEntity<String> updateProfile(@PathVariable Long id, @Valid @RequestBody DoctorProfileDto profileDto) {
        doctorService.updateDoctorProfile(id, profileDto);
        return ResponseEntity.ok("Profile updated successfully for doctor ID: " + id);
    }

    @PutMapping("/{id}/availability")
    @PreAuthorize("#id == @userRepository.findByEmail(principal.username).get().id")
    public ResponseEntity<SetAvailabilityResponseDto> setWeeklyAvailability(@PathVariable Long id, @Valid @RequestBody WeeklyAvailabilityRequestDto request) {
        SetAvailabilityResponseDto responseDto = doctorService.setWeeklyAvailability(id, request);
        return ResponseEntity.ok(responseDto);
    }

   
    @PutMapping("/{id}/appointments/{appointmentId}/reschedule")
    @PreAuthorize("#id == @userRepository.findByEmail(principal.username).get().id")
    public ResponseEntity<AppointmentResponseDto> rescheduleAppointment(
            @PathVariable Long id,
            @PathVariable Long appointmentId,
            @Valid @RequestBody RescheduleRequestDto request) {

        AppointmentResponseDto updatedAppointment = doctorService.rescheduleAppointment(id, appointmentId, request.getNewAppointmentDateTime());
        return ResponseEntity.ok(updatedAppointment);
    }

    @GetMapping("/{id}/appointments/upcoming")
    @PreAuthorize("#id == @userRepository.findByEmail(principal.username).get().id")
    public ResponseEntity<?> getUpcomingAppointments(@PathVariable Long id) {
        return doctorService.getUpcomingAppointmentsForDoctor(id);
    }

    @PutMapping("/{id}/appointments/{appointmentId}/confirm")
    @PreAuthorize("#id == @userRepository.findByEmail(principal.username).get().id")
    public ResponseEntity<AppointmentActionResponseDto> confirmAppointment(@PathVariable Long id, @PathVariable Long appointmentId) {
        AppointmentActionResponseDto responseDto = doctorService.updateAppointmentStatus(id, appointmentId, AppointmentStatus.CONFIRMED_BY_DOCTOR);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}/appointments/{appointmentId}/decline")
    @PreAuthorize("#id == @userRepository.findByEmail(principal.username).get().id")
    public ResponseEntity<AppointmentActionResponseDto> declineAppointment(@PathVariable Long id, @PathVariable Long appointmentId) {
        AppointmentActionResponseDto responseDto = doctorService.updateAppointmentStatus(id, appointmentId, AppointmentStatus.CANCELLED_BY_DOCTOR);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}/appointments/{appointmentId}/complete")
    @PreAuthorize("#id == @userRepository.findByEmail(principal.username).get().id")
    public ResponseEntity<AppointmentActionResponseDto> completeAppointment(@PathVariable Long id, @PathVariable Long appointmentId) {
        AppointmentActionResponseDto responseDto = doctorService.updateAppointmentStatus(id, appointmentId, AppointmentStatus.COMPLETED);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{id}/appointments/history")
    @PreAuthorize("#id == @userRepository.findByEmail(principal.username).get().id")
    public ResponseEntity<List<AppointmentHistoryDto>> getAppointmentHistory(@PathVariable Long id, @RequestParam(required = false) Long patientId) {
        List<AppointmentHistoryDto> history = doctorService.getAppointmentHistory(id, patientId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/{id}/appointments/{appointmentId}/notes")
    @PreAuthorize("#id == @userRepository.findByEmail(principal.username).get().id")
    public ResponseEntity<AddNoteResponseDto> addConsultationNote(@PathVariable Long id, @PathVariable Long appointmentId, @Valid @RequestBody ConsultationNoteDto noteDto) {
        AddNoteResponseDto response = doctorService.addConsultationNote(id, appointmentId, noteDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}