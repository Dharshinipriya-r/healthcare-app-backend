package com.hospital.Hospital.Management.controller;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
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
import org.springframework.web.bind.annotation.RestController;

import com.hospital.Hospital.Management.dto.ApiResponse;
import com.hospital.Hospital.Management.dto.AppointmentRequestDto;
import com.hospital.Hospital.Management.dto.AppointmentResponseDto;
import com.hospital.Hospital.Management.dto.BookingResponseDto;
import com.hospital.Hospital.Management.dto.RescheduleRequestDto;
import com.hospital.Hospital.Management.service.AppointmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/book")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<BookingResponseDto> bookAppointment(@Valid @RequestBody AppointmentRequestDto requestDto, Principal principal) {
        BookingResponseDto response = appointmentService.bookAppointment(requestDto, principal.getName());
        HttpStatus status = response.isSuccess() ? HttpStatus.CREATED : HttpStatus.CONFLICT;
        return new ResponseEntity<>(response, status);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse> cancelAppointment(@PathVariable Long id, Principal principal) throws AccessDeniedException {
        appointmentService.cancelAppointment(id, principal.getName());
        return ResponseEntity.ok(new ApiResponse(true, "Appointment cancelled successfully.", null));
    }

   
    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponseDto> rescheduleAppointment(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleRequestDto requestDto,
            Principal principal) throws AccessDeniedException {
        AppointmentResponseDto updatedAppointment = appointmentService.rescheduleAppointment(id, principal.getName(), requestDto.getNewAppointmentDateTime());
        return ResponseEntity.ok(updatedAppointment);
    }

    @GetMapping("/my-appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentResponseDto>> getMyPatientAppointments(Principal principal) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForPatient(principal.getName()));
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentResponseDto>> getUpcomingAppointments(Principal principal) {
        List<AppointmentResponseDto> upcomingAppointments = appointmentService.getUpcomingAppointmentsForPatient(principal.getName());
        return ResponseEntity.ok(upcomingAppointments);
    }

    @GetMapping("/upcoming/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<List<AppointmentResponseDto>> getAllUpcomingAppointments() {
        List<AppointmentResponseDto> upcomingAppointments = appointmentService.getAllUpcomingAppointments();
        return ResponseEntity.ok(upcomingAppointments);
    }

    @PostMapping("/send-reminders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> sendAppointmentReminders() {
        appointmentService.sendAppointmentReminders();
        return ResponseEntity.ok(new ApiResponse(true, "Appointment reminders sent successfully", null));
    }

    @PostMapping("/{id}/send-reminder")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse> sendSingleAppointmentReminder(@PathVariable Long id) {
        appointmentService.sendSingleAppointmentReminder(id);
        return ResponseEntity.ok(new ApiResponse(true, "Appointment reminder sent successfully", null));
    }
}