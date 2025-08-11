// src/main/java/com/hospital/Hospital/Management/doctor_management_module_3/dto/WeeklyAvailabilityRequestDto.java
package com.hospital.Hospital.Management.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class WeeklyAvailabilityRequestDto {
    @Valid
    private List<DoctorAvailabilityDto> availability;

    @NotNull(message = "Slot duration is required")
    @Min(value = 10, message = "Slot duration must be at least 10 minutes")
    private Integer slotDurationInMinutes; // <-- ADDED THIS FIELD
}