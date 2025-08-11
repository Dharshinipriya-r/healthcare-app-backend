package com.hospital.Hospital.Management.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A generic DTO for responding to actions a doctor takes on an appointment
 * (e.g., confirm, decline, complete).
 */
@Data
@Builder
public class AppointmentActionResponseDto {
    private Long appointmentId;
    private Long doctorId;
    private String doctorName;
    private Long patientId;
    private String patientName;
    private String newStatus;
    private String message;
    private LocalDateTime timestamp;
}