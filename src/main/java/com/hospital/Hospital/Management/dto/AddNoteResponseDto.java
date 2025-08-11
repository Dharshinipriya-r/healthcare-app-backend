package com.hospital.Hospital.Management.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddNoteResponseDto {
    private Long noteId;
    private Long appointmentId;
    private String message;
    private Long doctorId;
    private String doctorName;
    private Long patientId;
    private String patientName;
    private ConsultationNoteDto noteDetails;
}