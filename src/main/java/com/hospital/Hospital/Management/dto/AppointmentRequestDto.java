package com.hospital.Hospital.Management.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentRequestDto {
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Appointment date and time are required")
    @Future(message = "Appointment must be in the future")
    private LocalDateTime appointmentDateTime;
}