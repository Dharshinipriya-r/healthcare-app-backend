// src/main/java/com/hospital/Hospital/Management/doctor_management_module_3/dto/DoctorAvailabilityDto.java
package com.hospital.Hospital.Management.dto;

import com.hospital.Hospital.Management.model.DayOfWeek;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalTime;

@Data
public class DoctorAvailabilityDto {
    @NotNull
    private DayOfWeek dayOfWeek;
    @NotNull
    private LocalTime startTime;
    @NotNull
    private LocalTime endTime;
}