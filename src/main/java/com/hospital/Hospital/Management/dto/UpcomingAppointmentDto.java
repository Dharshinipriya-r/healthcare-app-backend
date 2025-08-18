package com.hospital.Hospital.Management.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpcomingAppointmentDto {
    private Long appointmentId;
    private String patientName;
    private LocalDateTime appointmentDateTime;
    private String status;
}