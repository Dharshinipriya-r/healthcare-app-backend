package com.hospital.Hospital.Management.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class WaitlistEntryDto {
    private Long waitlistId;
    private Long patientId;
    private String patientName;
    private LocalDate preferredDate;
    private LocalDateTime requestedAt;
}