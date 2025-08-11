package com.hospital.Hospital.Management.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AppointmentHistoryDto {
    private Long appointmentId;

    // --- ADDED DOCTOR DETAILS ---
    private Long doctorId;
    private String doctorName;

    private Long patientId;
    private String patientName;
    private LocalDateTime appointmentDateTime;
    private ConsultationNoteDto consultationNote;
}