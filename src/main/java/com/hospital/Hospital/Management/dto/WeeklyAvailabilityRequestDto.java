package com.hospital.Hospital.Management.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WeeklyAvailabilityRequestDto {
    @Valid
    private List<DoctorAvailabilityDto> availability;

    @NotNull(message = "Slot duration is required")
    @Min(value = 10, message = "Slot duration must be at least 10 minutes")
    private Integer slotDurationInMinutes; 
}