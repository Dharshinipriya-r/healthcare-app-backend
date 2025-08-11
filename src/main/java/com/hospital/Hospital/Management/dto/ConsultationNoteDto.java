// src/main/java/com/hospital/Hospital/Management/doctor_management_module_3/dto/ConsultationNoteDto.java
package com.hospital.Hospital.Management.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConsultationNoteDto {
    @NotBlank(message = "Diagnosis cannot be blank")
    private String diagnosis;
    @NotBlank(message = "Prescription cannot be blank")
    private String prescription;
    private String treatmentDetails;
    private String remarks;
}