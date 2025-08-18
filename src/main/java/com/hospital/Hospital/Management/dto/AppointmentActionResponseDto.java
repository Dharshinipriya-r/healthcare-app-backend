package com.hospital.Hospital.Management.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;


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