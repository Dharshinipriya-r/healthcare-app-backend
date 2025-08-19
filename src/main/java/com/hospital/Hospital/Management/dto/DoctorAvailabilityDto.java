package com.hospital.Hospital.Management.dto;

import java.time.LocalTime;

import com.hospital.Hospital.Management.model.DayOfWeek;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DoctorAvailabilityDto {
    @NotNull
    private DayOfWeek dayOfWeek;
    @NotNull
    private LocalTime startTime;
    @NotNull
    private LocalTime endTime;
}