package com.hospital.Hospital.Management.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppointmentHistoryDto {
    private Long appointmentId;

  
    private Long doctorId;
    private String doctorName;

    private Long patientId;
    private String patientName;
    private LocalDateTime appointmentDateTime;
    private ConsultationNoteDto consultationNote;
}