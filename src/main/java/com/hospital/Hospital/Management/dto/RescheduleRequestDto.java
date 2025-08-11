package com.hospital.Hospital.Management.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RescheduleRequestDto {

    @NotNull(message = "The new appointment date and time cannot be null.")
    @Future(message = "The new appointment must be in the future.")
    private LocalDateTime newAppointmentDateTime;
}