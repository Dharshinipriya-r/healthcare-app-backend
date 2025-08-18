package com.hospital.Hospital.Management.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppointmentResponseDto {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private LocalDateTime appointmentDateTime;
    private String status;
    private LocalDateTime createdAt;
    private ConsultationNoteDto consultationNote;
}