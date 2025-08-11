// src/main/java/com/hospital/Hospital/Management/doctor_management_module_3/dto/UpcomingAppointmentDto.java
package com.hospital.Hospital.Management.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UpcomingAppointmentDto {
    private Long appointmentId;
    private String patientName;
    private LocalDateTime appointmentDateTime;
    private String status;
}